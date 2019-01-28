package com.oneops.proxy.clients.proxy;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

import com.oneops.proxy.clients.model.DateAdapter;
import com.oneops.proxy.clients.model.ErrorRes;
import com.oneops.proxy.clients.model.Result;
import com.oneops.proxy.config.ClientsAuthConfig;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.utils.SecretsConstants;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.GeneralSecurityException;
import java.util.Optional;
import javax.net.ssl.*;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Tekton proxy client.
 *
 * @author Varsha
 */
public class TektonProxyClient {

  /**
   * Initializes the TektonProxy client
   *
   * @param config OneOpsConfig.TrustStore config properties.
   * @param clientsConfig ClientsAuthConfig clientsConfig properties.
   */
  private static final Logger log = LoggerFactory.getLogger(TektonProxyClient.class);

  private TektonProxy tektonProxy;

  private Converter<ResponseBody, ErrorRes> errResConverter;

  private OneOpsConfig.TrustStore config;

  private ClientsAuthConfig clientsConfig;

  public TektonProxyClient(OneOpsConfig.TrustStore config, ClientsAuthConfig clientsConfig) {
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

    log.info("Initialize setup for Tekton Proxy Client");

    /* Get the configuration properties for Tekton only for given domain*/
    Optional<ClientsAuthConfig.ClientsAuthDomain> clientAuth = clientsConfig.getAuthClients(domain);

    Moshi moshi = new Moshi.Builder().add(new DateAdapter()).build();

    HttpLoggingInterceptor logIntcp = new HttpLoggingInterceptor(s -> log.info(s));
    logIntcp.setLevel(BASIC);

    TrustManager[] trustManagers = ProxyClientUtil.getTrustManagers(config);
    SSLSocketFactory socketFactory = ProxyClientUtil.getSocketfactory(trustManagers);

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
                      SecretsConstants.TEKTON_AUTH_HEADER, clientAuth.get().getAuth().getToken());

                  return chain.proceed(reqBuilder.build());
                })
            .build();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(clientAuth.get().getUrl())
            .client(okhttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build();

    this.tektonProxy = retrofit.create(TektonProxy.class);
    errResConverter = retrofit.responseBodyConverter(ErrorRes.class, new Annotation[0]);
  }

  /**
   * Invoke authorization API for Managed Services.
   *
   * @param userName
   * @param org
   * @param project
   * @param env
   * @throws IOException throws if any IO error when communicating to Secrets Proxy.
   */
  public Result<Void> doAuth(String userName, String org, String project, String env)
      throws IOException {
    log.info("TektonProxyClient::auth::namespace::" + org + "_" + project + "_" + env);
    return exec(tektonProxy.auth(userName, org, project, env));
  }

  /** Helper method to handle {@link Call} object and return the execution {@link Result}. */
  private <T> Result<T> exec(Call<T> call) throws IOException, IllegalArgumentException {
    Response<T> res = call.execute();
    ErrorRes err = null;
    T body = null;
    Result<T> result = null;
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
