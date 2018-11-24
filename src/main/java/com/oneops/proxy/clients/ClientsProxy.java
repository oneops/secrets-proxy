package com.oneops.proxy.clients;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * API interface for clients proxy.
 *
 * @author Varsha
 */
public interface ClientsProxy {
  String prefix = "/v1/apps";

  String tekauthPrefix = "/v1/auth";

  @GET("/{user}/{appName}")
  Call<String> auth(@Path("user") String user, @Path("appName") String appName);
}
