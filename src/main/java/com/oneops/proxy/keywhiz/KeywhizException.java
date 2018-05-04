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
package com.oneops.proxy.keywhiz;

import java.io.IOException;

/**
 * A custom checked exception to report keywhiz errors.
 *
 * @author Suresh
 */
public class KeywhizException extends IOException {

  private final int statusCode;

  /**
   * A constructor.
   *
   * @param statusCode Http status code.
   * @param message Exception message.
   */
  public KeywhizException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  /**
   * Another constructor.
   *
   * @param statusCode Http status code.
   * @param message Exception message.
   * @param cause The cause.
   */
  public KeywhizException(int statusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  /** Http status code that caused this exception. */
  public int getStatusCode() {
    return statusCode;
  }
}
