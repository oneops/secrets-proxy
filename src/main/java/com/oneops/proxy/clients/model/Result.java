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
package com.oneops.proxy.clients.model;

/**
 * Holds secrets proxy response T and {@link ErrorRes} result.
 *
 * @author Varsha
 */
public class Result<T> {

  private final T body;

  private final ErrorRes err;

  private final int code;

  private final boolean isSuccessful;

  public Result(T body, ErrorRes err, int code, boolean isSuccessful) {
    this.body = body;
    this.err = err;
    this.code = code;
    this.isSuccessful = isSuccessful;
  }

  public T getBody() {
    return body;
  }

  public ErrorRes getErr() {
    return err;
  }

  public int getCode() {
    return code;
  }

  public boolean isSuccessful() {
    return isSuccessful;
  }

  @Override
  public String toString() {
    return "Result{"
        + "body="
        + body
        + ", err="
        + err
        + ", code="
        + code
        + ", isSuccessful="
        + isSuccessful
        + '}';
  }
}
