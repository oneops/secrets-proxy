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
package com.oneops.proxy.web;

import com.oneops.proxy.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * A rest controller advice to return {@link ErrorResponse} for a
 * particular controller and/or exception type
 *
 * @author Suresh
 */
@RestControllerAdvice
public class ErrorControllerAdvice {

    /**
     * An exception handler method for {@link IllegalArgumentException} thrown
     * from all the Rest controllers.
     *
     * @param req http request.
     * @param ex  exception thrown.
     * @return {@link ErrorResponse}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleControllerException(HttpServletRequest req, Throwable ex) {
        String path = getReqPath(req);
        return new ErrorResponse(System.currentTimeMillis(), BAD_REQUEST.value(), BAD_REQUEST.getReasonPhrase(), ex.getMessage(), path);
    }

    /**
     * Returns the http status from request.
     */
    private HttpStatus getStatus(HttpServletRequest req) {
        Integer statusCode = (Integer) req.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }

    /**
     * Returns the http request path.
     */
    private String getReqPath(HttpServletRequest req) {
        String path = (String) req.getAttribute("javax.servlet.error.request_uri");
        return path == null ? "" : path;
    }
}
