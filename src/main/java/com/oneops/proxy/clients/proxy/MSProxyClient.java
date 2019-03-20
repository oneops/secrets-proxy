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
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.model.ms.MSClientAuthRequest;
import com.oneops.proxy.model.ms.MSClientAuthResponse;
import com.oneops.proxy.utils.SecretsConstants;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.GeneralSecurityException;
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

  private MSProxy msProxy;

  private Converter<ResponseBody, ErrorRes> errResConverter;

  private OneOpsConfig.TrustStore config;

  private ClientsAuthConfig clientsConfig;

  public MSProxyClient(OneOpsConfig.TrustStore config, ClientsAuthConfig clientsConfig) {
    this.config = config;
    this.clientsConfig = clientsConfig;
  }

  /**
   * Initial client setup to get TrustStore, OkHttpClient and Retrofit
   *
   * @param domain
   * @throws GeneralSecurityException
   */
  public void setup(String domain) throws GeneralSecurityException {

    log.info("Initialize setup for MS Proxy Client");

    /* Get the clients configuration for MS*/
    Optional<ClientsAuthConfig.ClientsAuthDomain> clientAuth =
        clientsConfig.getAuthClients(SecretsConstants.MS + domain);

    Moshi moshi = new Moshi.Builder().add(new DateAdapter()).build();

    HttpLoggingInterceptor logIntcp = new HttpLoggingInterceptor(s -> log.info(s));
    logIntcp.setLevel(BASIC);

    TrustManager[] trustManagers = ProxyClientUtil.getTrustManagers(config);
    SSLSocketFactory socketFactory = ProxyClientUtil.getSocketfactory(trustManagers);

    int timeout = clientsConfig.getExpiresInSec();
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
            .build();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(clientAuth.get().getUrl())
            .client(okhttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build();

    this.msProxy = retrofit.create(MSProxy.class);
    errResConverter = retrofit.responseBodyConverter(ErrorRes.class, new Annotation[0]);
  }

  /**
   * Invoke authorization API for Managed Services.
   *
   * @param authRequest
   * @throws IOException throws if any IO error when communicating to Secrets Proxy.
   */
  public Result<MSClientAuthResponse> doAuth(MSClientAuthRequest authRequest) throws IOException {
    log.info("MSProxyClient::auth::create client request::" + authRequest.getNamespace());
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
      if (res.errorBody() != null && res.errorBody().contentLength() != 0) {
        err = errResConverter.convert(res.errorBody());
      }
    }
    return new Result<>(body, err, res.code(), res.isSuccessful());
  }
}
