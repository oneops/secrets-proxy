/**
 * *****************************************************************************
 *
 * <p>Copyright 2017 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.proxy.ldap;

import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.security.KeywhizKeyStore;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.*;
import org.ldaptive.*;
import org.ldaptive.auth.*;
import org.ldaptive.cache.*;
import org.ldaptive.pool.*;
import org.ldaptive.ssl.SslConfig;
import org.slf4j.*;
import sun.security.x509.X500Name;

/**
 * Ldap client for authenticating/searching AD accounts.
 *
 * <p>ToDo: Remove sun.security.x509.X500Name. This might be an issue in future java versions with
 * JPMS (Jigsaw).
 *
 * @author Suresh G
 */
public class LdapClient {

  private static final Logger log = LoggerFactory.getLogger(LdapClient.class);
  private static final String USERNAME_PATTERN = "[^A-Za-z0-9-_.]";
  private final ConnectionFactory pcf;
  private final OneOpsConfig.LDAP config;
  private final Cache<SearchRequest> cache;
  private final Authenticator auth;

  /** Checks if the user id is valid. */
  private static boolean isSanitizedUsername(String name) {
    return name.equals(name.replaceAll(USERNAME_PATTERN, ""));
  }

  /**
   * Initializes the LDAP client.
   *
   * @param config LDAP config properties.
   * @param keywhizKeyStore LDAP keystore.
   * @throws GeneralSecurityException
   */
  public LdapClient(OneOpsConfig.LDAP config, KeywhizKeyStore keywhizKeyStore)
      throws GeneralSecurityException {
    log.info("Initializing the LDAP client...");
    this.config = config;
    SslConfig ssl = new SslConfig();
    ssl.setTrustManagers(keywhizKeyStore.getTrustManagers());

    ConnectionConfig connConfig = new ConnectionConfig();
    connConfig.setLdapUrl(config.getServer());
    connConfig.setConnectTimeout(Duration.ofSeconds(5));
    connConfig.setResponseTimeout(Duration.ofSeconds(5));
    connConfig.setConnectionInitializer(
        new BindConnectionInitializer(config.getUserDn(), new Credential(config.getPassword())));
    connConfig.setSslConfig(ssl);
    // connConfig.setUseStartTLS(true);

    log.info("Creating blocking connection pool with LDAP bind.");
    DefaultConnectionFactory dcf = new DefaultConnectionFactory(connConfig);
    BlockingConnectionPool connPool = new BlockingConnectionPool(dcf);
    connPool.initialize();

    pcf = new PooledConnectionFactory(connPool);
    cache = new LRUCache<>(100, Duration.ofMinutes(30), Duration.ofMinutes(15));

    log.info("Initializing LDAP authenticator with cache.");
    SearchDnResolver dnResolver = new SearchDnResolver(pcf);
    dnResolver.setBaseDn(config.getUserBaseDn());
    dnResolver.setSubtreeSearch(true);
    dnResolver.setUserFilter(String.format("(%s={user})", config.getUserAttribute()));
    dnResolver.setSearchCache(cache);

    BindAuthenticationHandler authHandler = new BindAuthenticationHandler(dcf);
    auth = new Authenticator(dnResolver, authHandler);
    log.info("LDAP client initialization completed.");
  }

  /**
   * Searches for entries matching given user id.
   *
   * @param userId LDAP/AD user id
   * @return list of {@link X500Name} matching the given user id.
   * @throws LdapException if there are any errors searching LDAP or invalid user id.
   */
  public List<X500Name> searchUser(String userId) throws LdapException {
    if (!isSanitizedUsername(userId)) {
      throw new LdapException("Invalid user id: " + userId);
    }
    return search(userId, config.getUserBaseDn(), config.getUserAttribute());
  }

  /**
   * Searches role name for given userDN.
   *
   * @param userDN userDN
   * @return userDN roles.
   * @throws LdapException if there are any errors searching LDAP.
   */
  public @Nonnull List<String> getRoles(String userDN) throws LdapException {
    List<X500Name> names = search(userDN, config.getRoleBaseDn(), config.getRoleAttribute());
    return names
        .stream()
        .map(
            x -> {
              try {
                return x.getCommonName();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * List of LDAP roles to which access is allowed.
   *
   * @return lits of role names.
   */
  public @Nonnull List<String> requiredRoles() {
    return config.getRoles();
  }

  /**
   * Searches for entries matching given user id, baseDn and attribute.
   *
   * @param name LDAP/AD user id
   * @param baseDn user/role baseDn
   * @param attributeName attribute name to search for.
   * @return list of {@link X500Name} matching the given user id.
   * @throws LdapException if there are any errors searching LDAP or invalid user id.
   */
  private @Nonnull List<X500Name> search(String name, String baseDn, String attributeName)
      throws LdapException {
    SearchExecutor executor = new SearchExecutor();
    executor.setBaseDn(baseDn);
    executor.setSearchScope(SearchScope.SUBTREE);
    executor.setSearchCache(cache);
    // Use "*" to query all the attributes.
    SearchFilter filter = new SearchFilter(String.format("(%s=%s)", attributeName, name));
    SearchResult result = executor.search(pcf, filter).getResult();
    return result
        .getEntries()
        .stream()
        .map(
            entry -> {
              try {
                return new X500Name(entry.getDn());
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * Authenticates the user.
   *
   * @return The userDN of successfully authenticated userId else return <code>null</code>.
   */
  public @Nullable String authenticate(String userId, String password) throws LdapException {
    LdapEntry ldapEntry = authenticate(userId, password.toCharArray());
    if (ldapEntry != null) {
      return ldapEntry.getDn();
    }
    return null;
  }

  /**
   * Authenticates the AD/Ldap user.
   *
   * @return The {@link LdapEntry} of successfully authenticated userId else return <code>null
   *     </code>.
   */
  public @Nullable LdapEntry authenticate(String userId, char[] password) throws LdapException {
    if (!isSanitizedUsername(userId)) {
      throw new LdapException("Invalid user id: " + userId);
    }
    AuthenticationResponse response =
        auth.authenticate(new AuthenticationRequest(userId, new Credential(password)));
    if (response.getResult()) {
      log.debug(userId + " authentication succeeded.");
      return response.getLdapEntry();
    } else {
      log.error(userId + " authentication failed. LDAP response: " + response);
      return null;
    }
  }
}
