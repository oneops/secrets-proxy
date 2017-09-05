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

import com.oneops.proxy.model.RootResponse;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.oneops.proxy.config.Constants.APP_NAME;

/**
 * Application root controller.
 *
 * @author Suresh
 */
@RestController
@Api(value = "Root EndPoint", description = "Secrets Proxy and API doc info.")
public class RootController {

    /**
     * Maven artifact version.
     */
    @Value("${info.version}")
    private String version;

    /**
     * Application info.
     */
    @GetMapping(path = "/")
    @ApiOperation(value = "Version Info", notes = "Token header is not required for this request.")
    public RootResponse info() {
        return new RootResponse(APP_NAME, version);
    }

    /**
     * Redirect to Swagger API doc.
     */
    @GetMapping(path = "/apidocs")
    @ApiOperation(value = "Swagger API Doc", notes = "Token header is not required for this request.")
    public void apiDoc(HttpServletResponse res) throws IOException {
        res.sendRedirect("/swagger-ui.html");
    }
}
