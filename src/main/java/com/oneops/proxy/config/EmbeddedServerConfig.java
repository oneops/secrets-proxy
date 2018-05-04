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
package com.oneops.proxy.config;

import static com.oneops.proxy.gateway.ProxyServlet.InitParams.*;

import com.oneops.proxy.gateway.ProxyServlet;
import java.lang.management.ManagementFactory;
import java.util.*;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.*;

/**
 * Embedded jetty server configuration.
 *
 * @author Suresh
 */
@Configuration
public class EmbeddedServerConfig {

  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Configures the embedded jetty server. The values are configured in <b>application.yaml</b>
   * file.
   *
   * @param port jetty server port
   * @param maxThreads thread pool min thread
   * @param minThreads thread pool max thread
   * @param idleTimeout maximum thread idle time
   * @param jmxEnabled true, if jetty jmx is enabled.
   * @return {@link JettyEmbeddedServletContainerFactory}
   */
  @Bean
  public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(
      @Value("${server.port:8443}") final int port,
      @Value("${jetty.thread-pool.max-threads:200}") final int maxThreads,
      @Value("${jetty.thread-pool.min-threads:8}") final int minThreads,
      @Value("${jetty.thread-pool.idle-timeout:60000}") final int idleTimeout,
      @Value("${jetty.jmx.enabled:true}") final boolean jmxEnabled) {
    log.info("Configuring Jetty server.");
    final JettyEmbeddedServletContainerFactory factory =
        new JettyEmbeddedServletContainerFactory(port);
    factory.addServerCustomizers(
        server -> {
          final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
          threadPool.setMinThreads(minThreads);
          threadPool.setMaxThreads(maxThreads);
          threadPool.setIdleTimeout(idleTimeout);
          log.info("Server thread pool config:  " + server.getThreadPool());
          // Jetty JMX config.
          if (jmxEnabled) {
            log.info("Exposing Jetty managed beans to the JMX platform server.");
            server.addBean(new MBeanContainer(ManagementFactory.getPlatformMBeanServer()));
          }
        });
    return factory;
  }

  /**
   * Configures a custom jetty http proxy servlet based on <b>oneops.proxy.enabled</b> config
   * property. The proxy configuration is done on the <b>application.yaml</b> file.
   *
   * @param config OneOps config
   * @return {@link ServletRegistrationBean}
   */
  @Bean
  @ConditionalOnProperty("oneops.proxy.enabled")
  public ServletRegistrationBean registerProxyServlet(OneOpsConfig config) {
    log.info("OneOps Http Proxy is enabled.");
    OneOpsConfig.Proxy proxyCfg = config.getProxy();

    Map<String, String> initParams = new HashMap<>();
    initParams.put(proxyTo.name(), proxyCfg.getProxyTo());
    initParams.put(prefix.name(), proxyCfg.getPrefix());
    initParams.put(viaHost.name(), proxyCfg.getViaHost());
    initParams.put(trustAll.name(), String.valueOf(proxyCfg.isTrustAll()));
    initParams.put(xAuthHeader.name(), config.getAuth().getHeader());

    ServletRegistrationBean servletBean =
        new ServletRegistrationBean(new ProxyServlet(), proxyCfg.getPrefix() + "/*");
    servletBean.setName("OneOps Proxy Servlet");
    servletBean.setInitParameters(initParams);
    servletBean.setAsyncSupported(true);
    log.info("Configured OneOps proxy servlet with mapping: " + proxyCfg.getPrefix());
    return servletBean;
  }

  /**
   * Helper method set max post size for Jetty.
   *
   * @param maxHttpPostSize max post size in bytes
   * @param handlers Server handlers.
   */
  @SuppressWarnings("unused")
  private void setHandlerMaxHttpPostSize(int maxHttpPostSize, Handler... handlers) {
    for (Handler handler : handlers) {
      if (handler instanceof ContextHandler) {
        ((ContextHandler) handler).setMaxFormContentSize(maxHttpPostSize);
        ((ContextHandler) handler).setMaxFormKeys(1000);
      } else if (handler instanceof HandlerWrapper) {
        setHandlerMaxHttpPostSize(maxHttpPostSize, ((HandlerWrapper) handler).getHandler());
      } else if (handler instanceof HandlerCollection) {
        setHandlerMaxHttpPostSize(maxHttpPostSize, ((HandlerCollection) handler).getHandlers());
      }
    }
  }
}
