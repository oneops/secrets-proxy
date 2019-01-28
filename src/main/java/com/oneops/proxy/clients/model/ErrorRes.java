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
 * Secrets Proxy common error response.
 *
 * @author Varsha
 */
public class ErrorRes {

  private final long timestamp;
  private final int status;
  private final String error;
  private final String message;
  private final String path;

  public ErrorRes(long timestamp, int status, String error, String message, String path) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getStatus() {
    return status;
  }

  public String getError() {
    return error;
  }

  public String getMessage() {
    return message;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return "ErrorRes{"
        + "timestamp="
        + timestamp
        + ", status="
        + status
        + ", error='"
        + error
        + '\''
        + ", message='"
        + message
        + '\''
        + ", path='"
        + path
        + '\''
        + '}';
  }
}
