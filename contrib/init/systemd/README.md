## Keywhiz Proxy Env Vars

   All the external configurations to run **Keywhiz Proxy** server are set in the [keywhiz-proxy-env](keywhiz-proxy-env) 
   file. 
   
   >In OneOps production setup, the actual env configuration file is securely stored in [Keywhiz](https://github.com/square/keywhiz) 
   >and made available to running servers using Keywhiz client component under `/secrets` directory.
   
   The following are environment variables and it's description.
   
   ####  Secrets and Logs Config
   
   * **SECRETS_DIR** - The directory to look for secrets files. Defaults to `/secrets`.
   * **LOGGING_PATH** - The directory for application/access/audit and runtime logs. Defaults to `/log`.

   #### Keywhiz Proxy Server config
  
   * **ONEOPS_BASE_URL** - https://oneops.com/
   * **SERVER_SSL_KEY_STORE** - file:${SECRETS_DIR}/keywhiz_proxy_keystore.p12
   * **SERVER_SSL_KEY_PASSWORD** - xxxxx
   * **SERVER_SSL_KEY_STORE_PASSWORD** - xxxxx

   #### Keywhiz Server and Automation Client config
   
   * **ONEOPS_KEYWHIZ_BASE_URL** - https://keywhiz.com:4444/
   * **ONEOPS_KEYWHIZ_TRUST_STORE_PATH** - file:${SECRETS_DIR}/keywhiz_truststore.p12
   * **ONEOPS_KEYWHIZ_TRUST_STORE_STORE_PASSWORD** - xxxxx
   * **ONEOPS_KEYWHIZ_KEY_STORE_PATH** - file:${SECRETS_DIR}/keywhiz_keystore.p12
   * **ONEOPS_KEYWHIZ_KEY_STORE_STORE_PASSWORD** - xxxxx
   * **ONEOPS_KEYWHIZ_KEY_STORE_KEY_PASSWORD** - xxxxx

   #### AD/LDAP Server Config
   
   * **ONEOPS_LDAP_SERVER** - ldap://ldap.com
   * **ONEOPS_LDAP_USER_BASE_DN** - dc=xxxxx,dc=xxxxx,dc=com
   * **ONEOPS_LDAP_USER_DN=CN** - xxxxx,DC=xxxxx,DC=xxxxx,DC=com
   * **ONEOPS_LDAP_PASSWORD** - xxxxx
   * **ONEOPS_LDAP_TRUST_STORE_PATH** - file:${SECRETS_DIR}/ldap_truststore.p12
   * **ONEOPS_LDAP_TRUST_STORE_STORE_PASSWORD** - xxxxx

   #### Keywhiz Proxy Token Auth Config
   
   * **ONEOPS_AUTH_SIGNING_KEY** - xxxxx
   * **ONEOPS_AUTH_EXPIRES_IN_SEC** - xxxxx

   #### OneOps User Datasource config
   
   * **SPRING_DATASOURCE_URL** - jdbc:postgresql://userdb:5432/xxxxx
   * **SPRING_DATASOURCE_USERNAME** - xxxxx
   * **SPRING_DATASOURCE_PASSWORD** - xxxxx

   #### Management app config
   
   * **MANAGEMENT_USER** - xxxxx
   * **MANAGEMENT_PASSWORD** - xxxxx
