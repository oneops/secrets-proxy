package com.oneops.proxy.clients.connection;

import com.oneops.proxy.auth.user.OneOpsUser;
import com.oneops.proxy.clients.ClientsProxy;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.model.ErrorResponse;
import com.oneops.proxy.model.Result;
import com.oneops.proxy.utils.DateAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BASIC;

/**
 * Proxy client to connect Managed Services
 *
 * @author Varsha
 */
@Component
public class TektonClientService {

  private Logger log = Logger.getLogger(getClass().getSimpleName());

  private ClientsProxy clientsProxy;

  private Converter<ResponseBody, ErrorResponse> errResConverter;
  private String appName;
  private OneOpsUser user;

  public TektonClientService( OneOpsConfig config) throws GeneralSecurityException {

    HttpLoggingInterceptor logIntcp = new HttpLoggingInterceptor(s -> log.info(s));
    logIntcp.setLevel(BASIC);

    Moshi moshi = new Moshi.Builder().add(new DateAdapter()).build();

    OkHttpClient okhttp =
        new OkHttpClient()
            .newBuilder()
            .addInterceptor(
                chain -> {
                  Request.Builder reqBuilder =
                      chain
                          .request()
                          .newBuilder()
                          .addHeader("Authorization", config.getAuthorizationCode());

                  return chain.proceed(reqBuilder.build());
                })
            .hostnameVerifier((h, s) -> true)
            .build();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(config.getAuthenticationURL())// + "/" + user.getUsername() + appName.replace("_", "/"))
            .client(okhttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build();

    clientsProxy = retrofit.create(ClientsProxy.class);
  }

  /** Helper method to handle {@link Call} object and return the execution {@link Result}. */
  private <T> Result<T> exec(Call<T> call) throws IOException {
    Response<T> res = call.execute();
    ErrorResponse err = null;
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

  public Result<String> auth(OneOpsUser user, String appName) throws IOException {
      this.appName = appName;
      this.user = user;
    return exec(clientsProxy.auth(user.getUsername() ,appName));
  }
}
