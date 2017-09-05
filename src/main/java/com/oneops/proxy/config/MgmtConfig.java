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

import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.metrics.jmx.JmxMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.context.annotation.*;
import org.springframework.jmx.export.MBeanExporter;

/**
 * Application management bean configurations. Mainly used for configuring actuators.
 *
 * @author Suresh
 */
@Configuration
public class MgmtConfig {

    /**
     * Contribute SpringBoot version to "/info".
     *
     * @return {@link InfoContributor}
     */
    @Bean
    public InfoContributor versionInfo() {
        return builder -> builder.withDetail("spring-boot.version", SpringBootVersion.getVersion());
    }

    /**
     * Exports actuator metrics to JMX.
     */
    @Bean
    @ExportMetricWriter
    public MetricWriter metricWriter(MBeanExporter exporter) {
        return new JmxMetricWriter(exporter);
    }
}
