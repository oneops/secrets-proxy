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

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.slf4j.*;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.*;

/**
 * Cache manager customizer. The proxy application by default using the awesome Caffeine cache
 * provider.
 *
 * @author Suresh G
 */
@Configuration
@EnableCaching
public class CacheConfig {

  private final Logger log = LoggerFactory.getLogger(getClass());

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
   * Default cache spec configuration for all the caches. The default cache size is 200 and would
   * expire after a min (60sec) of write operation.
   *
   * @return {@link CaffeineSpec}
   */
  @Bean
  public CaffeineSpec caffeineSpec() {
    CaffeineSpec spec = CaffeineSpec.parse("maximumSize=200,expireAfterWrite=300s");
    log.info("Using CaffeineSpec " + spec.toParsableString());
    return spec;
  }
}
