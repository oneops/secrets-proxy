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
package com.oneops.proxy.utils;

import static java.time.format.DateTimeFormatter.ofPattern;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Secrets Proxy Json Date adapter.
 *
 * @author Varsha
 */
public class DateAdapter {

  private static final DateTimeFormatter formatter = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  @ToJson
  String toJson(LocalDateTime date) {
    return formatter.format(date);
  }

  @FromJson
  LocalDateTime fromJson(String date) {
    return LocalDateTime.from(formatter.parse(date));
  }
}
