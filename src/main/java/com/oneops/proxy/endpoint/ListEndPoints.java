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
package com.oneops.proxy.endpoint;

import com.oneops.proxy.config.OneOpsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A custom actuator endpoint (<b>/endpoints</b>) to list all other endpoints. This includes the
 * custom Http Proxy endpoint ({@link com.oneops.proxy.gateway.ProxyServlet}) details also.
 *
 * @author Suresh
 */
@Component
public class ListEndPoints extends AbstractEndpoint<Map<String, String>> {

  private String mgmtContext;

  private final List<Endpoint> endpoints;

  private final OneOpsConfig config;

  public ListEndPoints(
      @Value("${management.context-path}") String mgmtContext,
      OneOpsConfig config,
      List<Endpoint> endpoints) {
    super("endpoints", false, true);
    this.mgmtContext = mgmtContext;
    this.endpoints = endpoints;
    this.config = config;
  }

  @Override
  public Map<String, String> invoke() {
    Map<String, String> endPoints =
        endpoints
            .stream()
            .collect(Collectors.toMap(this::getEndPointID, ListEndPoints::isEnabled, (a, b) -> a));
    endPoints.put(config.getProxy().getPrefix(), String.valueOf(config.getProxy().isEnabled()));
    return endPoints;
  }

  private String getEndPointID(Endpoint e) {
    return String.format("%s/%s", mgmtContext, e.getId());
  }

  private static String isEnabled(Endpoint e) {
    return e.isEnabled() ? "enabled" : "disabled";
  }
}
