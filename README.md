<div align="center">

# :key: OneOps Secrets Proxy 

[![api-doc][apidoc-svg]][apidoc-url] [![java-doc][javadoc-svg]][javadoc-url] [![changelog][cl-svg]][cl-url] 

 A secure proxy service for managing [OneOps][oneops-url] secrets.
 
</div>

<img src="docs/images/keywhiz-proxy-arch.png" width=750 height=500>


## OneOps Secrets-Proxy Keystores

  The following Trust-stores/Keystores are used in Secrets-proxy application.
  
  - [keywhiz_proxy_keystore.p12](src/main/resources/keystores/keywhiz_proxy_keystore.p12) 
  
      The default TLS server certificate for Keywhiz-Proxy application. This is a self-signed TLS cert with 
      the following details.
          
      * Common Name    : `keywhiz-proxy.dev.oneops.com`
      * Key Password   : `kwproxy-password`
      * Store Password : `kwproxy-password` 
         
     ```
      For production deployment, contact your infosec team to get new TLS server certificate.
     ```
                 
  - [keywhiz_keystore.p12](src/main/resources/keystores/keywhiz_keystore.p12) 
  
      The keywhiz automation **mTLS client certificate**. Used for all the keywhiz server automation API calls.
      Make sure the client has been registered and `automationAllowed=true` on the keywhiz server. The client cert
      has to be trusted by the same RootCA used on Keywhiz server.
      
     ```
     For production deployment, contact your infosec/keywhiz server team to get new TLS client certificate.
     ```

  - [keywhiz_truststore.p12](src/main/resources/keystores/keywhiz_truststore.p12) 
  
      The keywhiz server trust-store, containing the trusted CA (Certificate Authorities) certs or cert chains. 
      Used for all the keywhiz server admin/automation API calls.
 
     ```
      For production deployment, use openssl to create trustore of your keywhiz server.
     ```
     or you can use tool like [InstallCerts](https://github.com/sureshg/InstallCerts) to auto-generate trust-store
     from the TLS endpoint.
     
                   
  - [ldap_truststore.p12](src/main/resources/keystores/ldap_truststore.p12) 
  
      Your LDAP/AD server trust-store, containing the trusted CA (Certificate Authorities) certs or cert chains. 
      Used for AD/LDAP user authentication APIs.
    
      ```
       For production deployment, use openssl to create trustore of your AD/LDAP server.
      ```
      or you can use tool like [InstallCerts](https://github.com/sureshg/InstallCerts) to auto-generate trust-store
      from the TLS endpoint.   

### Docker 

  * Build the image
    
    ```
     $ ./mvnw clean package
     $ docker build -t secrets-proxy:1.1.0 .
    ```  
  * Run 
  
    ```
     $ docker run -it --rm --name secrets-proxy -p 8443:8443  -e name=Secrets-Proxy -d secrets-proxy:1.1.0
     $ open https://localhost:8443/
    ``` 
  * Debugging and Logs
  
    ```
    $ docker exec -it secrets-proxy sh
    # cd log/
    /log # ls -ltrh
    total 64
    drwxr-xr-x    2 root     root        4.0K Aug  9 21:50 audit
    drwxr-xr-x    2 root     root        4.0K Aug  9 21:50 access
    -rw-r--r--    1 root     root       54.0K Aug  9 21:51 keywhiz-proxy.log
    ```       
      
    ```
    set -o allexport
    source conf-file
    set +o allexport
    ```
    
### Generate JOOQ source.

```bash
 $ ./mvnw clean package -P generate
 # $ ./mvnw versions:display-dependency-updates
```

### Keysync

   [Keysync](https://github.com/square/keysync) is the keywhiz client used on computes to sync secrets. Inorder to build keysync,
   
   ```
   # Make sure to install go (https://golang.org/dl/)
   # export GOOS=linux
   $ mkdir ~/tmp
   $ export GOPATH=$HOME/tmp
   $ go get -u github.com/square/keysync
   $ cd $GOPATH/src/github.com/square/keysync
   $go build -o keysync ./cmd/keysync/
   $ ./keysync
   ```
   
#### Misc
 
  * Secrets Tools - https://github.com/oneops/secrets-proxy/releases/tag/tools
  * JWT Token Verification - https://jwt.io/
  * [REST API References](https://news.ycombinator.com/item?id=11971491)   

#### TODO

 * Move the automation client to Retrofit.
 * X509 Authentication ??
 * Update to the latest keysync.
 * Feature toggles implementation.
 * Http2/Grpc (May be with JDK 9)
 * JTI claim to maintain list of blacklisted or revoked tokens.
 * Springboot admin integration.


### Why we chose Spring Boot

 * https://twitter.com/springcentral/status/878264199729860608
 * https://twitter.com/fintanr/status/877988573399531520
 * https://www.jetbrains.com/research/devecosystem-2017/java/ (Check the Web framework section)
 
 -----------------
 <sup><b>**</b></sup>Require [Java 8 or later][java-download]
 
 <!-- Badges -->
 
 [apidoc-url]: https://oneops.github.com/secrets-proxy/apidocs
 [apidoc-svg]: https://img.shields.io/badge/api--doc-latest-green.svg?style=flat-square
 
 [javadoc-url]: https://oneops.github.com/secrets-proxy/javadocs
 [javadoc-svg]: https://img.shields.io/badge/java--doc-latest-ff69b4.svg?style=flat-square
 
 [cl-url]: https://github.com/oneops/secrets-proxy/blob/master/CHANGELOG.md
 [cl-svg]: https://img.shields.io/badge/change--log-latest-blue.svg?style=flat-square
 
 [oneops-url]: http://oneops.com/developer/index.html
 [java-download]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
 