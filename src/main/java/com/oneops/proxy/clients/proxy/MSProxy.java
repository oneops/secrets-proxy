package com.oneops.proxy.clients.proxy;

import com.oneops.proxy.model.ms.MSClientAuthRequest;
import com.oneops.proxy.model.ms.MSClientAuthResponse;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Managed Service client Proxy
 *
 * @author Varsha
 */
public interface MSProxy {

  String authPrefix = "api/ms/auth/";

  @POST(authPrefix)
  Call<MSClientAuthResponse> auth(@Body MSClientAuthRequest req);
}
