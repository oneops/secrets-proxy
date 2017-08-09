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
package com.oneops.proxy.model;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;

import java.util.Date;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Login failure error response.
 *
 * @author Suresh G
 */
public class ErrorResponse {

    private final long timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    /**
     * Error response constructor.
     *
     * @param timestamp A time stamp when the error occurred.
     * @param status    The HTTP status code
     * @param error     The HTTP status code description
     * @param message   A message which elaborates the error further
     * @param path      The path that was requested
     */
    public ErrorResponse(long timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Error response constructor from {@link ErrorAttributes}
     *
     * @param errAttrs error attributes.
     */
    public ErrorResponse(Map<String, Object> errAttrs) {
        Object ts = errAttrs.get("timestamp");
        timestamp = ts instanceof Date ? ((Date) ts).getTime() : System.currentTimeMillis();
        Object st = errAttrs.get("status");
        status = st instanceof Integer ? (int) st : INTERNAL_SERVER_ERROR.value();
        Object err = errAttrs.get("error");
        error = (err != null) ? err.toString() : "None";
        Object msg = errAttrs.get("message");
        message = (msg != null) ? msg.toString() : "None";
        Object pt = errAttrs.get("path");
        path = (pt != null) ? pt.toString() : "";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "timestamp=" + timestamp +
                ", status=" + status +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
