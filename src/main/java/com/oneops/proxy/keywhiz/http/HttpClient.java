/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.proxy.keywhiz.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.oneops.proxy.keywhiz.KeywhizClient;
import com.oneops.security.XsrfTokenInterceptor;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieManager;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.logging.Logger;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.HttpHeaders.USER_AGENT;
import static java.net.CookiePolicy.ACCEPT_ALL;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * An abstract http client for both Keywhiz automation and admin clients.
 *
 * @author Suresh
 */
public abstract class HttpClient {

    protected static final Logger log = Logger.getLogger(KeywhizClient.class.getSimpleName());

    protected static final MediaType JSON = MediaType.parse("application/json");

    protected static final ObjectMapper mapper = createObjectMapper();

    protected final OkHttpClient client;

    protected final HttpUrl baseUrl;

    private CookieManager cookieMgr;

    /**
     * Creates the http client.
     *
     * @param baseUrl keywhiz server base url
     * @throws GeneralSecurityException
     */
    protected HttpClient(String baseUrl) throws GeneralSecurityException {
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

    private TrustManager[] loadTrustMaterial() throws GeneralSecurityException {
        KeyStore trustStore = KeywhizKeyStore.getTrustStore();
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    private KeyManager[] loadKeyMaterial() throws GeneralSecurityException {
        if (isClientAuthEnabled()) {
            log.info("Client auth is enabled. Loading the keystore...");
            KeyStore keystore = KeywhizKeyStore.getKeyStore();
            final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, KeywhizKeyStore.getKeyPassword());
            return kmfactory.getKeyManagers();
        } else {
            log.warning("Client auth is disabled. Skipping keystore.");
            return new KeyManager[0];
        }
    }

    /**
     * Creates a {@link OkHttpClient} to start a TLS connection.
     */
    protected OkHttpClient createHttpsClient() throws GeneralSecurityException {
        TrustManager[] trustManagers = loadTrustMaterial();
        KeyManager[] keyManagers = loadKeyMaterial();
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(log::info);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder client = new OkHttpClient().newBuilder()
                .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
                .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .connectTimeout(5, SECONDS)
                .readTimeout(10, SECONDS)
                .writeTimeout(10, SECONDS)
                .addInterceptor(chain -> {
                    Request req = chain.request().newBuilder()
                            .addHeader(CONTENT_TYPE, JSON.toString())
                            .addHeader(USER_AGENT, "OneOps-Keywhiz-Cli")
                            .build();
                    return chain.proceed(req);
                })
                .addInterceptor(loggingInterceptor)
                .addNetworkInterceptor(new XsrfTokenInterceptor());

        if (!isClientAuthEnabled()) {
            log.info("Client auth is disabled. Configuring the cookie manager!");
            cookieMgr = new CookieManager();
            cookieMgr.setCookiePolicy(ACCEPT_ALL);
            client.cookieJar(new JavaNetCookieJar(cookieMgr));
        }
        return client.build();
    }

    /**
     * Clear all cookies from cookie manager.
     */
    public void clearCookies() {
        if (!isClientAuthEnabled()) {
            log.warning("Clearing all cookies!");
            cookieMgr.getCookieStore().removeAll();
        } else {
            log.warning("Cookie is not enabled for client auth.");
        }
    }

    /**
     * Check if client auth is enabled (mTLS) instead of session cookie.
     *
     * @return <code>true</code> if client auth is enabled
     */
    public abstract boolean isClientAuthEnabled();

    /**
     * Maps some of the common HTTP errors to the corresponding exceptions.
     */
    protected void throwOnCommonError(int status) throws IOException {
        switch (status) {
            case HttpStatus.SC_BAD_REQUEST:
                throw new MalformedRequestException();
            case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
                throw new UnsupportedMediaTypeException();
            case HttpStatus.SC_NOT_FOUND:
                throw new NotFoundException();
            case HttpStatus.SC_UNAUTHORIZED:
                throw new UnauthorizedException();
            case HttpStatus.SC_FORBIDDEN:
                throw new ForbiddenException();
            case HttpStatus.SC_CONFLICT:
                throw new ConflictException();
            case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                throw new ValidationException();
        }
        if (status >= 400) {
            throw new IOException("Unexpected status code on response: " + status);
        }
    }

    protected String makeCall(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        try {
            throwOnCommonError(response.code());
        } catch (IOException e) {
            response.body().close();
            throw e;
        }
        return response.body().string();
    }

    protected String httpGet(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return makeCall(request);
    }

    protected String httpPost(HttpUrl url, Object content) throws IOException {
        RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(content));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader(CONTENT_TYPE, JSON.toString())
                .build();

        return makeCall(request);
    }

    protected String httpPut(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(MediaType.parse("text/plain"), ""))
                .build();

        return makeCall(request);
    }

    protected String httpDelete(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        return makeCall(request);
    }


    public static class MalformedRequestException extends IOException {

        @Override
        public String getMessage() {
            return "Malformed request syntax from client (400)";
        }
    }

    public static class UnauthorizedException extends IOException {

        @Override
        public String getMessage() {
            return "Not allowed to login, password may be incorrect (401)";
        }
    }

    public static class ForbiddenException extends IOException {

        @Override
        public String getMessage() {
            return "Resource forbidden (403)";
        }
    }

    public static class NotFoundException extends IOException {

        @Override
        public String getMessage() {
            return "Resource not found (404)";
        }
    }

    public static class UnsupportedMediaTypeException extends IOException {

        @Override
        public String getMessage() {
            return "Resource media type is incorrect or incompatible (415)";
        }
    }

    public static class ConflictException extends IOException {

        @Override
        public String getMessage() {
            return "Conflicting resource (409)";
        }
    }

    public static class ValidationException extends IOException {

        @Override
        public String getMessage() {
            return "Malformed request semantics from client (422)";
        }
    }

}
