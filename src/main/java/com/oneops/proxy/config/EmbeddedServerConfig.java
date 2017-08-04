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
package com.oneops.proxy.config;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;

/**
 * Embedded jetty server configuration.
 *
 * @author Suresh
 */
@Configuration
public class EmbeddedServerConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedServerConfig.class);

    /**
     * Configures the embedded jetty server. The values are configured in
     * <b>application.yaml</b> file.
     *
     * @param port        jetty server port
     * @param maxThreads  thread pool min thread
     * @param minThreads  thread pool max thread
     * @param idleTimeout maximum thread idle time
     * @return {@link JettyEmbeddedServletContainerFactory}
     */
    @Bean
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(@Value("${server.port:8443}") final int port,
                                                                                     @Value("${jetty.thread-pool.max-threads:200}") final int maxThreads,
                                                                                     @Value("${jetty.thread-pool.min-threads:8}") final int minThreads,
                                                                                     @Value("${jetty.thread-pool.idle-timeout:60000}") final int idleTimeout,
                                                                                     @Value("${jetty.jmx.enabled:true}") final boolean jmxEnabled) {
        log.info("Configuring Jetty server.");
        final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory(port);
        factory.addServerCustomizers(server -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMinThreads(minThreads);
            threadPool.setMaxThreads(maxThreads);
            threadPool.setIdleTimeout(idleTimeout);
            log.info("Server thread pool config:  " + server.getThreadPool());
            if (jmxEnabled) {
                log.info("Exposing Jetty managed beans to the JMX platform server.");
                server.addBean(new MBeanContainer(ManagementFactory.getPlatformMBeanServer()));
            }
        });
        return factory;
    }

    /**
     * Helper method set max post size for Jetty.
     *
     * @param maxHttpPostSize max post size in bytes
     * @param handlers        Server handlers.
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
