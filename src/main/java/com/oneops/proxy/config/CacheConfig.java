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

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache manager customizer. The proxy application by default
 * using the awesome Caffeine cache provider.
 *
 * @author Suresh G
 */
@Configuration
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Tunes the auto configured Cache manager.
     *
     * @return {@link CacheManagerCustomizer}
     */
    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer() {
        return cacheManager -> cacheManager.setAllowNullValues(false);
    }

    /**
     * Default cache spec configuration for all the caches. The default cache
     * size is 100 and would expire after a min (60sec) of write operation.
     *
     * @return {@link CaffeineSpec}
     */
    @Bean
    public CaffeineSpec caffeineSpec() {
        CaffeineSpec spec = CaffeineSpec.parse("maximumSize=100,expireAfterWrite=60s");
        log.info("Using CaffeineSpec " + spec.toParsableString());
        return spec;
    }
}
