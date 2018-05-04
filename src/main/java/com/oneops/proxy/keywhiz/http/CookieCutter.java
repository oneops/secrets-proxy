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
package com.oneops.proxy.keywhiz.http;

import okhttp3.Cookie;

import java.util.*;

/**
 * Cookie utility functions.
 *
 * @author Suresh
 */
public class CookieCutter {

  private CookieCutter() {}

  /**
   * Convert a request header to OkHttp's cookies. That extra step handles multiple cookies in a
   * single request header, which {@link Cookie#parse} doesn't support.
   */
  public static List<Cookie> decodeCookies(String httpHeader, String host) {
    List<Cookie> result = new ArrayList<>();
    for (int pos = 0, limit = httpHeader.length(), pairEnd; pos < limit; pos = pairEnd + 1) {
      pairEnd = delimiterOffset(httpHeader, pos, limit, ";,");
      int equalsSign = delimiterOffset(httpHeader, pos, pairEnd, '=');
      String name = trimSubstring(httpHeader, pos, equalsSign);
      if (name.startsWith("$")) continue;

      // We have either name=value or just a name.
      String value = equalsSign < pairEnd ? trimSubstring(httpHeader, equalsSign + 1, pairEnd) : "";

      // If the value is "quoted", drop the quotes.
      if (value.startsWith("\"") && value.endsWith("\"")) {
        value = value.substring(1, value.length() - 1);
      }

      result.add(new Cookie.Builder().name(name).value(value).domain(host).build());
    }
    return result;
  }

  /**
   * Returns the index of the first character in {@code input} that contains a character in {@code
   * delimiters}. Returns limit if there is no such character.
   */
  private static int delimiterOffset(String input, int pos, int limit, String delimiters) {
    for (int i = pos; i < limit; i++) {
      if (delimiters.indexOf(input.charAt(i)) != -1) return i;
    }
    return limit;
  }

  /**
   * Returns the index of the first character in {@code input} that is {@code delimiter}. Returns
   * limit if there is no such character.
   */
  private static int delimiterOffset(String input, int pos, int limit, char delimiter) {
    for (int i = pos; i < limit; i++) {
      if (input.charAt(i) == delimiter) return i;
    }
    return limit;
  }

  /**
   * Increments {@code pos} until {@code input[pos]} is not ASCII whitespace. Stops at {@code
   * limit}.
   */
  private static int skipLeadingAsciiWhitespace(String input, int pos, int limit) {
    for (int i = pos; i < limit; i++) {
      switch (input.charAt(i)) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
          continue;
        default:
          return i;
      }
    }
    return limit;
  }

  /**
   * Decrements {@code limit} until {@code input[limit - 1]} is not ASCII whitespace. Stops at
   * {@code pos}.
   */
  private static int skipTrailingAsciiWhitespace(String input, int pos, int limit) {
    for (int i = limit - 1; i >= pos; i--) {
      switch (input.charAt(i)) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
          continue;
        default:
          return i + 1;
      }
    }
    return pos;
  }

  /** Equivalent to {@code string.substring(pos, limit).trim()}. */
  public static String trimSubstring(String string, int pos, int limit) {
    int start = skipLeadingAsciiWhitespace(string, pos, limit);
    int end = skipTrailingAsciiWhitespace(string, start, limit);
    return string.substring(start, end);
  }
}
