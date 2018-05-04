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
package com.oneops.proxy.metrics;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;
import org.springframework.stereotype.Service;

/**
 * Some utility functions for timer and size.
 *
 * @author Suresh G
 */
@Service
public class MetricsUtilService {

  private DropwizardMetricServices metricService;

  public MetricsUtilService(DropwizardMetricServices metricService) {
    this.metricService = metricService;
  }

  /** A helper method to time operations or errors. */
  public <T, E extends Exception> T time(String metric, ThrowingSupplier<T, E> lambda) throws E {
    long start = currentTimeMillis();
    try {
      return lambda.get();
    } catch (Exception ex) {
      metricService.increment("meter." + metric + ".err");
      throw ex;
    } finally {
      metricService.submit("timer." + metric, currentTimeMillis() - start);
    }
  }

  /**
   * Converts a given number to a string preceded by the corresponding binary International System
   * of Units (SI) prefix.
   */
  public static String binaryPrefix(long size) {
    long unit = 1000;
    String suffix = "B";

    if (size < unit) {
      return format("%d %s", size, suffix);
    } else {
      String prefix = "KMGTPEZY";
      int exp = (int) (Math.log(size) / Math.log(unit));
      // Binary Prefix mnemonic that is prepended to the units.
      String binPrefix = prefix.charAt(exp - 1) + suffix;
      // Count => (unit^0.x * unit^exp)/unit^exp
      return format("%.2f %s", size / Math.pow(unit, exp), binPrefix);
    }
  }
}
