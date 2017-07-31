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
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Custom global error handler for rendering {@link ErrorAttributes} for this
 * application. All errors without any specific {@link ExceptionHandler} are
 * handled by ${{@link #errorResponse(HttpServletRequest, HttpServletResponse)}}
 * method.
 *
 * @author Suresh
 */
@Component
public class ErrorController extends BasicErrorController {

    public ErrorController(ErrorAttributes errAttrs, ServerProperties serverProperties) {
        super(errAttrs, serverProperties.getError());
    }

    /**
     * Handles all the errors and returns an {@link ErrorResponse}.
     *
     * @param req http request which caused some error.
     * @param res http response.
     * @return error response entity.
     */
    @RequestMapping(produces = "application/json")
    public @ResponseBody
    ResponseEntity<ErrorResponse> errorResponse(HttpServletRequest req, HttpServletResponse res) {
        Map<String, Object> body = getErrorAttributes(req, isIncludeStackTrace(req, MediaType.ALL));
        HttpStatus status = getStatus(req);
        ErrorResponse errRes = new ErrorResponse(body);
        return new ResponseEntity<>(errRes, status);
    }
}
