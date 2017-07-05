/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.proxy.ldap;


import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.auth.*;
import org.ldaptive.ssl.SslConfig;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.Duration;

/**
 * LDAP client.
 *
 * @author Suresh G
 */
public class LDAPClient {


    public static void main(String[] args) throws Exception {

        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        LDAP ldap = CliConfig.ldap;

        InputStream ins = KeywhizKeyStore.class.getResourceAsStream(ldap.getTrustStore().getName());
        System.out.println("Inputstream " + ins);
        KeyStore trustStore = KeyStore.getInstance(ldap.getTrustStore().getType());
        trustStore.load(ins, ldap.getTrustStore().getPassword());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        SslConfig ssl = new SslConfig();
        ssl.setTrustManagers(trustManagers);

        ConnectionConfig connConfig = new ConnectionConfig();
        connConfig.setUseStartTLS(true);
        connConfig.setConnectTimeout(Duration.ofSeconds(5));
        connConfig.setLdapUrl(ldap.getServer());
        connConfig.setResponseTimeout(Duration.ofSeconds(10));
        connConfig.setConnectionInitializer(new BindConnectionInitializer(ldap.getUserDN(), new Credential(ldap.getPassword())));
        connConfig.setSslConfig(ssl);

        SearchDnResolver dnResolver = new SearchDnResolver(new DefaultConnectionFactory(connConfig));
        dnResolver.setBaseDn("DC=xxxx,DC=com");
        dnResolver.setSubtreeSearch(true);
        dnResolver.setUserFilter("(sAMAccountName={user})");
        //dnResolver.setReferralHandler(new SearchReferralHandler());
        BindAuthenticationHandler authHandler = new BindAuthenticationHandler(new DefaultConnectionFactory(connConfig));
        Authenticator auth = new Authenticator(dnResolver, authHandler);
        AuthenticationResponse response = auth.authenticate(new AuthenticationRequest("xxx", new Credential("xxx")));
        System.out.println(response);
        if (response.getResult()) {
            System.out.println("OK");
            // authentication succeeded
        } else {
            System.out.println("Failed!");
            // authentication failed
        }
    }

}
