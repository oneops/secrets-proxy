package com.oneops.proxy.clients.connection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneops.proxy.keywhiz.http.HttpClient;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * @author Varsha
 */
public class ProxyClientConnection{

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    protected static final MediaType JSON = MediaType.parse("application/json");

    protected static final ObjectMapper mapper = createObjectMapper();

    protected  OkHttpClient client;

    protected  HttpUrl baseUrl;

    protected ProxyClientConnection() {}

    protected Response makeCall(Request request) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();

        } catch (IOException e) {
            response.body().close();
            throw e;
        }
        return response;
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Creates a {@link OkHttpClient} to start a TLS connection. The OKHttp logging is enabled if the
     * debug log is enabled for {@link HttpClient}.
     */
    protected OkHttpClient createHttpsClientWithSSLContext() throws GeneralSecurityException {

        TrustManager[] trustManagers = getTestAllTrustManager();

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustManagers, new SecureRandom());
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        OkHttpClient client = new OkHttpClient().newBuilder()
            .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
            .hostnameVerifier((h, s) -> true)
            .build();

        return client;

    }

    /**
     * Creates a {@link OkHttpClient} to start a TLS connection. The OKHttp logging is enabled if the
     * debug log is enabled for {@link HttpClient}.
     */
    protected OkHttpClient createHttpsClient() throws GeneralSecurityException {

        OkHttpClient client = new OkHttpClient().newBuilder()
            .build();

        return client;
    }

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

    protected Request httpPost(HttpUrl url, Object content) throws IOException {
        RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(content));
        Request request =
            new Request.Builder().url(url).post(body).addHeader(CONTENT_TYPE, JSON.toString()).

                build();

        return request;
    }

    protected Request httpGet(HttpUrl url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();

        return request;
    }
}
