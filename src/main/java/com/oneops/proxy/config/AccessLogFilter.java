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

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.List;

import static ch.qos.logback.core.spi.FilterReply.NEUTRAL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * A simple access filter to remove the management health calls (<b>/health</b>). We could have used
 * {@link ch.qos.logback.access.boolex.JaninoEventEvaluator}, but require additional dependencies
 * and our use case is simple. The filter along with <code>ignorePaths</code> is configured in
 * <b>logback-access.xml</b>.
 *
 * @author Suresh
 * @see <a href="https://logback.qos.ch/manual/filters.html#logbac-access"></a>
 */
public class AccessLogFilter extends AbstractMatcherFilter<AccessEvent> {

  /** Comma separated request paths, to be ignored from logging into access log. */
  private String ignorePaths;

  private List<String> paths;

  public void start() {
    if (this.ignorePaths != null) {
      super.start();
    }
  }

  public String getIgnorePaths() {
    return ignorePaths;
  }

  public void setIgnorePaths(String ignorePaths) {
    this.ignorePaths = ignorePaths;
    if (ignorePaths == null) {
      paths = emptyList();
    } else {
      paths = asList(this.ignorePaths.split(","));
    }
  }

  @Override
  public FilterReply decide(AccessEvent event) {
    if (!isStarted()) {
      return NEUTRAL;
    }

    if (paths.stream().anyMatch(path -> event.getRequestURI().matches(path))) {
      return onMatch;
    } else {
      return onMismatch;
    }
  }
}
