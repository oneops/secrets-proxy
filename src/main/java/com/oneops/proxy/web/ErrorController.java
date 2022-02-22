/**
 * *****************************************************************************
 *
 * <p>Copyright 2017 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.proxy.web;

import static org.springframework.http.MediaType.*;
import com.oneops.proxy.model.ErrorResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import javax.servlet.http.*;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.http.*;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * Custom global error handler for rendering {@link ErrorAttributes} for this application. All
 * errors without any specific {@link ExceptionHandler} are handled by ${{@link
 * #errorResponse(HttpServletRequest, HttpServletResponse)}} method.
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
  @RequestMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
  public @ResponseBody ResponseEntity<ErrorResponse> errorResponse(
      HttpServletRequest req, HttpServletResponse res) {
    Map<String, Object> body = getErrorAttributes(req, isIncludeStackTrace(req, MediaType.ALL));

    // Update status code and format error respose for RequestRejectedException
    Object message = "";
    Object ex = body.get("exception");
    String exceptionName = ex != null ? ex.toString() : "";

    if (exceptionName.contains(RequestRejectedException.class.getSimpleName())) {
      try {
        message = URLDecoder.decode(body.get("message").toString(), "UTF-8");
      } catch (UnsupportedEncodingException ignore) {
      }
      message = message.toString().replace(exceptionName + ":", "");
      body.put("message", message);
      body.put("status", HttpStatus.BAD_REQUEST.value());
      body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
      req.setAttribute("javax.servlet.error.status_code", HttpStatus.BAD_REQUEST.value());
    }

    HttpStatus status = getStatus(req);
    ErrorResponse errRes = new ErrorResponse(body);
    return new ResponseEntity<>(errRes, status);
  }
}
