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
package com.oneops.proxy.keywhiz.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.oneops.proxy.keywhiz.KeywhizException;
import com.oneops.proxy.security.KeywhizKeyStore;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.slf4j.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieManager;
import java.security.*;

import static com.google.common.net.HttpHeaders.*;
import static com.oneops.proxy.keywhiz.http.HttpStatus.*;
import static java.net.CookiePolicy.ACCEPT_ALL;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * An abstract http client for both Keywhiz automation and admin clients.
 *
 * @author Suresh
 */
public abstract class HttpClient {

  private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

  protected static final MediaType JSON = MediaType.parse("application/json");

  protected static final ObjectMapper mapper = createObjectMapper();

  protected final OkHttpClient client;

  protected final HttpUrl baseUrl;

  protected final KeywhizKeyStore keywhizKeyStore;

  private CookieManager cookieMgr;

  /**
   * Creates an http client.
   *
   * @param baseUrl keywhiz base url
   * @param keywhizKeyStore keywhiz keystore.
   * @throws GeneralSecurityException
   */
  protected HttpClient(String baseUrl, KeywhizKeyStore keywhizKeyStore)
      throws GeneralSecurityException {
    this.keywhizKeyStore = keywhizKeyStore;
    log.info("Creating Keywhiz client for " + baseUrl);
    this.client = createHttpsClient();
    this.baseUrl = HttpUrl.parse(baseUrl);
  }

  /**
   * Creates and customizes new ObjectMapper for common settings.
   *
   * @return customized input factory
   */
  public static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModules(new Jdk8Module());
    mapper.registerModules(new JavaTimeModule());
    mapper.registerModules(new GuavaModule());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }

  private KeyManager[] loadKeyMaterial() throws GeneralSecurityException {
    if (isClientAuthEnabled()) {
      return keywhizKeyStore.getKeyManagers();
    } else {
      log.warn("Client auth is disabled. Skipping keystore.");
      return new KeyManager[0];
    }
  }

  /**
   * Creates a {@link OkHttpClient} to start a TLS connection. The OKHttp logging is enabled if the
   * debug log is enabled for {@link HttpClient}.
   */
  protected OkHttpClient createHttpsClient() throws GeneralSecurityException {
    TrustManager[] trustManagers = keywhizKeyStore.getTrustManagers();
    KeyManager[] keyManagers = loadKeyMaterial();
    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
    sslContext.init(keyManagers, trustManagers, new SecureRandom());
    SSLSocketFactory socketFactory = sslContext.getSocketFactory();

    HttpLoggingInterceptor loggingInterceptor =
        new HttpLoggingInterceptor(
            msg -> {
              if (log.isDebugEnabled()) {
                log.debug(msg);
              }
            });
    loggingInterceptor.setLevel(Level.BASIC);

    OkHttpClient.Builder client =
        new OkHttpClient()
            .newBuilder()
            .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
            .connectionSpecs(singletonList(ConnectionSpec.MODERN_TLS))
            .followSslRedirects(false)
            .retryOnConnectionFailure(true)
            .connectTimeout(5, SECONDS)
            .readTimeout(5, SECONDS)
            .writeTimeout(5, SECONDS)
            .addInterceptor(
                chain -> {
                  Request req =
                      chain
                          .request()
                          .newBuilder()
                          .addHeader(CONTENT_TYPE, JSON.toString())
                          .addHeader(USER_AGENT, "OneOps-Secrets-Proxy")
                          .build();
                  return chain.proceed(req);
                })
            .addInterceptor(loggingInterceptor);

    if (!isClientAuthEnabled()) {
      log.info("Client auth is disabled. Configuring the cookie manager and XSRF interceptor.");
      cookieMgr = new CookieManager();
      cookieMgr.setCookiePolicy(ACCEPT_ALL);
      client
          .cookieJar(new JavaNetCookieJar(cookieMgr))
          .addNetworkInterceptor(new XsrfTokenInterceptor());
    }
    return client.build();
  }

  /** Clear all cookies from cookie manager. */
  public void clearCookies() {
    if (!isClientAuthEnabled()) {
      log.warn("Clearing all cookies!");
      cookieMgr.getCookieStore().removeAll();
    } else {
      log.warn("Cookie is not enabled for client auth.");
    }
  }

  /**
   * Check if client auth is enabled (mTLS) instead of session cookie.
   *
   * @return <code>true</code> if client auth is enabled
   */
  public abstract boolean isClientAuthEnabled();

  /** Maps some of the common HTTP errors to the corresponding exceptions. */
  protected void throwOnCommonError(int status, String message) throws IOException {
    switch (status) {
      case SC_BAD_REQUEST:
        throw new KeywhizException(status, "Malformed request syntax from client.");
      case SC_UNSUPPORTED_MEDIA_TYPE:
        throw new KeywhizException(status, "Resource media type is incorrect or incompatible.");
      case SC_NOT_FOUND:
        throw new KeywhizException(status, "Resource not found.");
      case SC_UNAUTHORIZED:
        throw new KeywhizException(status, "Not allowed to login, password may be incorrect.");
      case SC_FORBIDDEN:
        throw new KeywhizException(status, "Resource forbidden.");
      case SC_CONFLICT:
        throw new KeywhizException(status, "Resource already exists. Conflicting resource.");
      case SC_UNPROCESSABLE_ENTITY:
        throw new KeywhizException(status, "Malformed request semantics from client.");
    }
    if (status >= 400) {
      throw new KeywhizException(status, "Unknown Error: " + message);
    }
  }

  protected String makeCall(Request request) throws IOException {
    Response response = client.newCall(request).execute();
    try {
      throwOnCommonError(response.code(), response.message());
    } catch (IOException e) {
      response.body().close();
      throw e;
    }
    return response.body().string();
  }

  protected String httpGet(HttpUrl url) throws IOException {
    Request request = new Request.Builder().url(url).get().build();

    return makeCall(request);
  }

  protected String httpPost(HttpUrl url, Object content) throws IOException {
    RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(content));
    Request request =
        new Request.Builder().url(url).post(body).addHeader(CONTENT_TYPE, JSON.toString()).build();

    return makeCall(request);
  }

  protected String httpPut(HttpUrl url, Object content) throws IOException {
    RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(content));
    Request request = new Request.Builder().url(url).put(body).build();
    return makeCall(request);
  }

  protected String httpDelete(HttpUrl url) throws IOException {
    Request request = new Request.Builder().url(url).delete().build();

    return makeCall(request);
  }
}
