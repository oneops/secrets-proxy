package com.oneops.proxy.clients.proxy;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TektonProxy {

  String authPrefix = "api/v1/access";

  @GET(authPrefix + "/{username}/{org}/{project}/{env}")
  Call<Void> auth(
      @Path("username") String username,
      @Path("org") String org,
      @Path("project") String project,
      @Path("env") String env);
}
