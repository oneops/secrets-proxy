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
package com.oneops.proxy.keywhiz.security;

import com.google.common.net.HttpHeaders;
import okhttp3.Cookie;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.oneops.proxy.keywhiz.http.CookieCutter.decodeCookies;

/**
 * Http request interceptor to handle server-side XSRF protection.
 * If the server set a cookie with a specified name, the client will
 * send a header with each request with a specified name and value of
 * the server-supplied cookie.
 *
 * @author Suresh
 */

public class XsrfTokenInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(XsrfTokenInterceptor.class);

    private String xsrfCookieName = "XSRF-TOKEN";
    private String xsrfHeaderName = "X-XSRF-TOKEN";

    public XsrfTokenInterceptor() {
    }

    public XsrfTokenInterceptor(String xsrfCookieName, String xsrfHeaderName) {
        this.xsrfCookieName = xsrfCookieName;
        this.xsrfHeaderName = xsrfHeaderName;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        try {
            for (String header : req.headers(HttpHeaders.COOKIE)) {
                for (Cookie cookie : decodeCookies(header, req.url().host())) {
                    if (cookie.name().equalsIgnoreCase(xsrfCookieName)) {
                        req = req.newBuilder().addHeader(xsrfHeaderName, cookie.value()).build();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Error setting " + xsrfHeaderName + " header in request", ex);
        }
        return chain.proceed(req);
    }

    public String getXsrfCookieName() {
        return xsrfCookieName;
    }

    public String getXsrfHeaderName() {
        return xsrfHeaderName;
    }
}

