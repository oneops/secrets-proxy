package com.oneops.proxy.clients.proxy;

import static java.util.concurrent.TimeUnit.SECONDS;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

import com.oneops.proxy.clients.model.DateAdapter;
import com.oneops.proxy.clients.model.ErrorRes;
import com.oneops.proxy.clients.model.Result;
import com.oneops.proxy.config.ClientsAuthConfig;
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
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class TektonProxyClient {

  private Logger log = Logger.getLogger(getClass().getSimpleName());

  private ClientsAuthConfig config;

  private TektonProxy tektonProxy;

  private Converter<ResponseBody, ErrorRes> errResConverter;

  public TektonProxyClient(ClientsAuthConfig config, com.oneops.proxy.model.AppGroup appGroup)
      throws GeneralSecurityException {

    log.info("Initializing the Managed Service Client");
    this.config = config;

    /* Get the domainName from AppGroup.
     *  Output domainStr =  "tektonprod"
     */
    String domainStr = appGroup.getDomain().getType();

    log.info("Tekton Proxy Client :" + " Domain name=" + domainStr);

    /* Get the configuration properties for Tekton only for given domain*/
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
            //            .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
            //            .connectionSpecs(singletonList(ConnectionSpec.MODERN_TLS))
            //            .followSslRedirects(false)
            //            .retryOnConnectionFailure(true) //TODO : in Tekton dev SSLContext doesn't
            // work while oconnecting
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

    if (clientAuth.isPresent()) {

      Retrofit retrofit =
          new Retrofit.Builder()
              .baseUrl(clientAuth.get().getUrl())
              .client(okhttp)
              .addConverterFactory(MoshiConverterFactory.create(moshi))
              .build();

      this.tektonProxy = retrofit.create(TektonProxy.class);
      errResConverter = retrofit.responseBodyConverter(ErrorRes.class, new Annotation[0]);
    }
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
    return exec(tektonProxy.auth(userName, org, project, env));
  }

  /** Helper method to handle {@link Call} object and return the execution {@link Result}. */
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
