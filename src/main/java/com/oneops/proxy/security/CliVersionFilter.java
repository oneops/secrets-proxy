package com.oneops.proxy.security;

import com.oneops.proxy.config.OneOpsConfig;
import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * A security filter to validate compatible secrets cli version.
 *
 * @author Suresh
 */
public class CliVersionFilter extends GenericFilterBean {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private OneOpsConfig.Cli cliConfig;

  public CliVersionFilter(OneOpsConfig config) {
    this.cliConfig = config.getKeywhiz().getCli();
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpReq = asHttp(req);
    HttpServletResponse httpRes = asHttp(res);

    Optional<String> uaHeader = Optional.ofNullable(httpReq.getHeader("User-Agent"));
    if (uaHeader.isPresent()) {
      String userAgent = uaHeader.get();
      String userAgentPrefix = cliConfig.getUserAgentHeader();
      if (userAgent.startsWith(userAgentPrefix)) {
        String version = userAgent.replaceFirst(userAgentPrefix, "");
        if (!Objects.equals(version, cliConfig.getVersion())) {
          String msg =
              "You are using an old version of secrets cli (v"
                  + version
                  + "). Please download the latest version from "
                  + cliConfig.getDownloadUrl();
          log.error("CLI version {} not supported.", version);
          SecurityContextHolder.clearContext();
          httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
          return;
        }
      }
    }
    chain.doFilter(req, res);
  }

  private HttpServletRequest asHttp(ServletRequest request) {
    return (HttpServletRequest) request;
  }

  private HttpServletResponse asHttp(ServletResponse response) {
    return (HttpServletResponse) response;
  }
}
