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
package com.oneops.proxy.web;

import static com.oneops.proxy.config.Constants.CLI_CTLR_BASE_PATH;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.oneops.proxy.config.OneOpsConfig;
import com.oneops.proxy.keywhiz.KeywhizException;
import io.swagger.annotations.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * A rest controller to download secrets cli.
 *
 * @author Suresh G
 */
@RestController
@RequestMapping(CLI_CTLR_BASE_PATH)
@Api(value = "Secrets CLI Endpoint", description = "Secrets CLI details.")
public class CliController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private String cliVersion;

  private Path cliPath;

  public CliController(OneOpsConfig config) {
    OneOpsConfig.Cli cli = config.getKeywhiz().getCli();
    cliVersion = cli.getVersion();
    cliPath = Paths.get(cli.getDownloadPath());
    log.info("Created CLI controller for " + cli);
  }

  /**
   * Returns the authenticated current user info.
   *
   * @return OneOps user details.
   */
  @GetMapping("/info")
  @ApiOperation(value = "Secrets CLI Info", notes = "Secrets CLI details.")
  public Map<String, String> info() {
    Map<String, String> info = new HashMap<>(2);
    info.put("name", cliPath.getFileName().toString());
    info.put("version", cliVersion);
    return info;
  }

  /**
   * Download secrets cli binary. For better performance, the file resource is cached in memory (for
   * 5 min, default cache timeout) if it's less than 10 MB.
   *
   * @return file resource.
   * @throws IOException throws if any error reading the file.
   */
  @GetMapping("/download")
  @Cacheable("secrets-cli")
  @ApiOperation(value = "Download Secrets CLI latest version.")
  public ResponseEntity<Resource> download() throws IOException {
    File file = cliPath.toFile();
    if (file.exists() && file.isFile() && file.length() < 10_000_000) {
      log.info("Downloading the secrets cli.");
      ByteArrayResource bar = new ByteArrayResource(Files.readAllBytes(cliPath));
      return ResponseEntity.ok()
          .header("Content-disposition", "attachment;filename=" + file.getName())
          .contentLength(file.length())
          .contentType(MediaType.parseMediaType("application/octet-stream"))
          .body(bar);

    } else {
      log.error(format("Invalid secrets cli binary %s. Size: %d bytes.", cliPath, file.length()));
      throw new KeywhizException(
          INTERNAL_SERVER_ERROR.value(), "Latest secret cli binary is not available on server.");
    }
  }
}
