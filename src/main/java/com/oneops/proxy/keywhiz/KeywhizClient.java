/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oneops.proxy.keywhiz;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.keywhiz.http.*;
import com.oneops.proxy.keywhiz.model.*;
import com.oneops.proxy.security.KeywhizKeyStore;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import okhttp3.*;

/**
 * Client for interacting with the Keywhiz Server.
 *
 * <p>Facilitates the manipulation of Clients, Groups, Secrets and the connections between them.
 */
public class KeywhizClient extends HttpClient {

  /**
   * Create a keywhiz client for the given baseurl.
   *
   * @param keywhiz server base url
   * @param keywhizKeyStore keywhiz keystore.
   * @throws GeneralSecurityException throws if any error creating the https client.
   */
  public KeywhizClient(KeywhizKeyStore keywhizKeyStore, OneOpsConfig.Keywhiz keywhiz)
      throws GeneralSecurityException {
    super(keywhizKeyStore, keywhiz);
  }

  @Override
  public boolean isClientAuthEnabled() {
    return false;
  }

  /**
   * Login to the Keywhiz server.
   *
   * <p>Future requests made using this client instance will be authenticated.
   *
   * @param username login username
   * @param password login password
   * @throws IOException if a network IO error occurs
   */
  public void login(String username, char[] password) throws IOException {
    httpPost(baseUrl.resolve("/admin/login"), LoginRequest.from(username, password));
  }

  public List<Group> allGroups() throws IOException {
    String response = httpGet(baseUrl.resolve("/admin/groups/"));
    return mapper.readValue(response, new TypeReference<List<Group>>() {});
  }

  public GroupDetailResponse createGroup(
      String name, String description, ImmutableMap<String, String> metadata) throws IOException {
    checkArgument(!name.isEmpty());
    String response =
        httpPost(
            baseUrl.resolve("/admin/groups"), new CreateGroupRequest(name, description, metadata));
    return mapper.readValue(response, GroupDetailResponse.class);
  }

  public GroupDetailResponse groupDetailsForId(long groupId) throws IOException {
    String response = httpGet(baseUrl.resolve(format("/admin/groups/%d", groupId)));
    return mapper.readValue(response, GroupDetailResponse.class);
  }

  public void deleteGroupWithId(long groupId) throws IOException {
    httpDelete(baseUrl.resolve(format("/admin/groups/%d", groupId)));
  }

  public List<SanitizedSecret> allSecrets() throws IOException {
    String response = httpGet(baseUrl.resolve("/admin/secrets?nameOnly=1"));
    return mapper.readValue(response, new TypeReference<List<SanitizedSecret>>() {});
  }

  public List<SanitizedSecret> allSecretsBatched(int idx, int num, boolean newestFirst)
      throws IOException {
    String response =
        httpGet(
            baseUrl.resolve(
                String.format(
                    "/admin/secrets?idx=%d&num=%d&newestFirst=%s", idx, num, newestFirst)));
    return mapper.readValue(response, new TypeReference<List<SanitizedSecret>>() {});
  }

  public SecretDetailResponse createSecret(
      String name,
      String description,
      byte[] content,
      ImmutableMap<String, String> metadata,
      long expiry)
      throws IOException {
    checkArgument(!name.isEmpty());
    checkArgument(content.length > 0, "Content must not be empty");

    String b64Content = Base64.getEncoder().encodeToString(content);
    CreateSecretRequest request =
        new CreateSecretRequest(name, description, b64Content, metadata, expiry);
    String response = httpPost(baseUrl.resolve("/admin/secrets"), request);
    return mapper.readValue(response, SecretDetailResponse.class);
  }

  public SecretDetailResponse updateSecret(
      String name,
      boolean descriptionPresent,
      String description,
      boolean contentPresent,
      byte[] content,
      boolean metadataPresent,
      ImmutableMap<String, String> metadata,
      boolean expiryPresent,
      long expiry)
      throws IOException {
    checkArgument(!name.isEmpty());

    String b64Content = Base64.getEncoder().encodeToString(content);
    PartialUpdateSecretRequestV2 request =
        PartialUpdateSecretRequestV2.builder()
            .descriptionPresent(descriptionPresent)
            .description(description)
            .contentPresent(contentPresent)
            .content(b64Content)
            .metadataPresent(metadataPresent)
            .metadata(metadata)
            .expiryPresent(expiryPresent)
            .expiry(expiry)
            .build();
    String response =
        httpPost(baseUrl.resolve(format("/admin/secrets/%s/partialupdate", name)), request);
    return mapper.readValue(response, SecretDetailResponse.class);
  }

  public SecretDetailResponse secretDetailsForId(long secretId) throws IOException {
    String response = httpGet(baseUrl.resolve(format("/admin/secrets/%d", secretId)));
    return mapper.readValue(response, SecretDetailResponse.class);
  }

  public List<SanitizedSecret> listSecretVersions(String name, int idx, int numVersions)
      throws IOException {
    String response =
        httpGet(
            baseUrl.resolve(
                format(
                    "/admin/secrets/versions/%s?versionIdx=%d&numVersions=%d",
                    name, idx, numVersions)));
    return mapper.readValue(response, new TypeReference<List<SanitizedSecret>>() {});
  }

  public SecretDetailResponse rollbackSecret(String name, long version) throws IOException {
    String response =
        httpPost(baseUrl.resolve(format("/admin/secrets/rollback/%s/%d", name, version)), null);
    return mapper.readValue(response, SecretDetailResponse.class);
  }

  public void deleteSecretWithId(long secretId) throws IOException {
    httpDelete(baseUrl.resolve(format("/admin/secrets/%d", secretId)));
  }

  public List<Client> allClients() throws IOException {
    String httpResponse = httpGet(baseUrl.resolve("/admin/clients/"));
    return mapper.readValue(httpResponse, new TypeReference<List<Client>>() {});
  }

  public ClientDetailResponse createClient(String name) throws IOException {
    checkArgument(!name.isEmpty());
    String response = httpPost(baseUrl.resolve("/admin/clients"), new CreateClientRequest(name));
    return mapper.readValue(response, ClientDetailResponse.class);
  }

  public ClientDetailResponse clientDetailsForId(long clientId) throws IOException {
    String response = httpGet(baseUrl.resolve(format("/admin/clients/%d", clientId)));
    return mapper.readValue(response, ClientDetailResponse.class);
  }

  public void deleteClientWithId(long clientId) throws IOException {
    httpDelete(baseUrl.resolve(format("/admin/clients/%d", clientId)));
  }

  public void enrollClientInGroupByIds(long clientId, long groupId) throws IOException {
    httpPut(
        baseUrl.resolve(format("/admin/memberships/clients/%d/groups/%d", clientId, groupId)), "");
  }

  public void evictClientFromGroupByIds(long clientId, long groupId) throws IOException {
    httpDelete(
        baseUrl.resolve(format("/admin/memberships/clients/%d/groups/%d", clientId, groupId)));
  }

  public void grantSecretToGroupByIds(long secretId, long groupId) throws IOException {
    httpPut(
        baseUrl.resolve(format("/admin/memberships/secrets/%d/groups/%d", secretId, groupId)), "");
  }

  public void revokeSecretFromGroupByIds(long secretId, long groupId) throws IOException {
    httpDelete(
        baseUrl.resolve(format("/admin/memberships/secrets/%d/groups/%d", secretId, groupId)));
  }

  public Client getClientByName(String name) throws IOException {
    checkArgument(!name.isEmpty());
    String response =
        httpGet(
            baseUrl.resolve("/admin/clients").newBuilder().addQueryParameter("name", name).build());
    return mapper.readValue(response, Client.class);
  }

  public Group getGroupByName(String name) throws IOException {
    checkArgument(!name.isEmpty());
    String response =
        httpGet(
            baseUrl.resolve("/admin/groups").newBuilder().addQueryParameter("name", name).build());
    return mapper.readValue(response, Group.class);
  }

  public SanitizedSecret getSanitizedSecretByName(String name) throws IOException {
    checkArgument(!name.isEmpty());
    String response =
        httpGet(
            baseUrl.resolve("/admin/secrets").newBuilder().addQueryParameter("name", name).build());
    return mapper.readValue(response, SanitizedSecret.class);
  }

  public boolean isLoggedIn() throws IOException {
    HttpUrl url = baseUrl.resolve("/admin/me");
    Call call = client.newCall(new Request.Builder().get().url(url).build());
    return call.execute().code() != HttpStatus.SC_UNAUTHORIZED;
  }
}
