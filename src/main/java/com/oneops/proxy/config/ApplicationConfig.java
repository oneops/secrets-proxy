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
package com.oneops.proxy.config;

import static com.oneops.proxy.security.KeywhizKeyStore.Name.Keywhiz;
import static com.oneops.proxy.security.KeywhizKeyStore.Name.LDAP;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.proxy.keywhiz.KeywhizAutomationClient;
import com.oneops.proxy.keywhiz.KeywhizClient;
import com.oneops.proxy.ldap.LdapClient;
import com.oneops.proxy.security.KeywhizKeyStore;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Keywhiz proxy application java config.
 *
 * @author Suresh
 */
@Configuration
public class ApplicationConfig {

  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * A {@link BeanFactoryPostProcessor} to validate the properties.
   *
   * @return {@link PropertyVerifier}
   */
  @Bean
  public static PropertyVerifier propertyVerifierBean() {
    return new PropertyVerifier();
  }

  /**
   * Show application arguments.
   *
   * @param config {@link OneOpsConfig}
   * @return {@link CommandLineRunner}
   */
  @Bean
  public CommandLineRunner init(OneOpsConfig config) {
    return args -> {
      log.info("Starting OneOps Secret Management Server...");
      log.info("Application config, " + config);
      String cliArgs = String.join(", ", args);
      log.info("Application arguments are, " + ((cliArgs.isEmpty()) ? "N/A" : cliArgs));
    };
  }

  /**
   * Returns the keystrore for keywhiz server
   *
   * @param config Keywhiz config properties.
   * @param loader resource loader.
   * @return {@link KeywhizKeyStore}
   */
  @Bean(name = "keywhizKeyStore")
  public KeywhizKeyStore keywhizKeyStore(OneOpsConfig config, ResourceLoader loader) {
    OneOpsConfig.Keywhiz keywhiz = config.getKeywhiz();
    return new KeywhizKeyStore(Keywhiz, keywhiz.getTrustStore(), keywhiz.getKeyStore(), loader);
  }

  /**
   * Returns the keystrore for LDAP server
   *
   * @param config LDAP config properties.
   * @param loader resource loader.
   * @return {@link KeywhizKeyStore}
   */
  @Bean(name = "ldapKeyStore")
  public KeywhizKeyStore ldapKeyStore(OneOpsConfig config, ResourceLoader loader) {
    OneOpsConfig.LDAP ldap = config.getLdap();
    return new KeywhizKeyStore(LDAP, ldap.getTrustStore(), ldap.getKeyStore(), loader);
  }

  /** Returns the keywhiz http client */
  @Bean
  public KeywhizClient keywhizClient(
      OneOpsConfig config, @Qualifier("keywhizKeyStore") KeywhizKeyStore keywhizKeyStore)
      throws GeneralSecurityException {
    OneOpsConfig.Keywhiz keywhiz = config.getKeywhiz();
    return new KeywhizClient(keywhizKeyStore, keywhiz);
  }

  /** Returns the keywhiz automation client */
  @Bean
  public KeywhizAutomationClient keywhizAutomationClient(
      OneOpsConfig config, @Qualifier("keywhizKeyStore") KeywhizKeyStore keywhizKeyStore)
      throws GeneralSecurityException {
    OneOpsConfig.Keywhiz keywhiz = config.getKeywhiz();
    return new KeywhizAutomationClient(keywhizKeyStore, keywhiz);
  }

  /** Returns the LDAP client. */
  @Bean
  @Lazy
  public LdapClient ldapClient(
      OneOpsConfig config, @Qualifier("ldapKeyStore") KeywhizKeyStore keywhizKeyStore)
      throws GeneralSecurityException {
    return new LdapClient(config.getLdap(), keywhizKeyStore);
  }

  /**
   * Json (de)serializer config.
   *
   * @return Object mapper.
   */
  @Bean
  public ObjectMapper objectMapper() {
    return new Jackson2ObjectMapperBuilder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build();
  }
}
