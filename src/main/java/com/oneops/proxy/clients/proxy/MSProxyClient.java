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
package com.oneops.proxy.clients.proxy;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

import com.oneops.proxy.clients.model.DateAdapter;
import com.oneops.proxy.clients.model.ErrorRes;
import com.oneops.proxy.clients.model.Result;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.model.ms.MSClientAuthRequest;
import com.oneops.proxy.model.ms.MSClientAuthResponse;
import com.oneops.proxy.utils.SecretsConstants;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.logging.Logger;
import javax.net.ssl.*;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.*;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Managed Services proxy client.
 *
 * @author Varsha
 */
public class MSProxyClient {

  private Logger log = Logger.getLogger(getClass().getSimpleName());

  private ClientsAuthConfig config;

  private MSProxy msProxy;

  private Converter<ResponseBody, ErrorRes> errResConverter;

  public MSProxyClient(ClientsAuthConfig config, com.oneops.proxy.model.AppGroup appGroup)
      throws GeneralSecurityException {

    log.info("Initializing the Managed Service Client");
    this.config = config;

    /* create domain for managed services : output :: domainStr = "msdev"*/
    String domainStr = SecretsConstants.MS_APP + appGroup.getDomain().getType();

    /* Get the clients configuration for MS*/
    Optional<ClientsAuthConfig.ClientsAuthDomain> clientAuth =
        config.getConfigs().stream().filter(x -> x.getDomainNames().equals(domainStr)).findFirst();

    Moshi moshi = new Moshi.Builder().add(new DateAdapter()).build();

    HttpLoggingInterceptor logIntcp = new HttpLoggingInterceptor(s -> log.info(s));
    logIntcp.setLevel(BASIC);

    /* TODO : need to remove : changed to getTrustManagers() */
    TrustManager[] trustManagers = getTestAllTrustManager();
    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
    sslContext.init(null, trustManagers, new SecureRandom());
    SSLSocketFactory socketFactory = sslContext.getSocketFactory();

    int timeout = clientAuth.get().getTimeout();
    OkHttpClient okhttp =
        new OkHttpClient()
            .newBuilder()
            .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
            .connectionSpecs(singletonList(ConnectionSpec.MODERN_TLS))
            .followSslRedirects(false)
            .retryOnConnectionFailure(true)
            .connectTimeout(timeout, SECONDS)
            .readTimeout(timeout, SECONDS)
            .writeTimeout(timeout, SECONDS)
            .addNetworkInterceptor(logIntcp)
            .addInterceptor(
                chain -> {
                  Request.Builder reqBuilder =
                      chain.request().newBuilder().addHeader("Content-Type", "application/json");

                  reqBuilder.addHeader(
                      SecretsConstants.MS_AUTH_HEADER, clientAuth.get().getAuth().getToken());

                  return chain.proceed(reqBuilder.build());
                })
            .hostnameVerifier((h, s) -> true) /* TODO : Needs to remove*/
            .build();

    if (clientAuth.isPresent()) {

      Retrofit retrofit =
          new Retrofit.Builder()
              .baseUrl(clientAuth.get().getUrl())
              .client(okhttp)
              .addConverterFactory(MoshiConverterFactory.create(moshi))
              .build();

      this.msProxy = retrofit.create(MSProxy.class);
      errResConverter = retrofit.responseBodyConverter(ErrorRes.class, new Annotation[0]);
    }
  }

  /**
   * Invoke authorization API for Managed Services.
   *
   * @param authRequest
   * @throws IOException throws if any IO error when communicating to Secrets Proxy.
   */
  public Result<MSClientAuthResponse> doAuth(MSClientAuthRequest authRequest) throws IOException {
    return exec(msProxy.auth(authRequest));
  }

  /** Helper method to handle {@Param Call} object and return the execution {@link Result}. */
  private <T> Result<T> exec(Call<T> call) throws IOException {
    Response<T> res = call.execute();
    ErrorRes err = null;
    T body = null;

    if (res.isSuccessful()) {
      body = res.body();
    } else {
      if (res.errorBody() != null) {
        err = errResConverter.convert(res.errorBody());
      }
    }
    return new Result<>(body, err, res.code(), res.isSuccessful());
  }

  /** Return new trust managers from the trust-store. */
  private TrustManager[] getTrustManagers() throws GeneralSecurityException {
    final TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    // trustManagerFactory.init(keyStoreFromResource(config.getTrustStore()));
    return trustManagerFactory.getTrustManagers();
  }

  /* TODO : need to remove this */
  private TrustManager[] getTestAllTrustManager() {
    final TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {}

            @Override
            public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {}

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return new java.security.cert.X509Certificate[] {};
            }
          }
        };
    return trustAllCerts;
  }
}
