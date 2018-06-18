package com.oneops.proxy.model;

import static org.junit.Assert.assertEquals;

import com.oneops.proxy.authz.AuthDomain;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Test app secrets.
 *
 * @author Suresh
 */
public class AppSecretTest {

  @Test
  public void getUniqSecretName() throws Exception {

    AppGroup appGroup1 = new AppGroup(AuthDomain.PROD, "oneops_my-app1_prod");
    AppGroup appGroup2 = new AppGroup(AuthDomain.MGMT, "oneops_my-app2_prod");

    System.out.println(appGroup1);
    System.out.println(appGroup2);
    List<String> secretNames =
        Arrays.asList("db-password.txt", "db@password.txt", "db:password.txt", "db_password.txt");
    secretNames.forEach(
        secret -> {
          AppSecret secret1 = new AppSecret(secret, appGroup1);
          AppSecret secret2 = new AppSecret(secret, appGroup2);

          assertEquals(secret, secret1.getSecretName());
          assertEquals(secret, secret2.getSecretName());

          assertEquals(secret, new AppSecret(secret1.getUniqSecretName()).getSecretName());
          assertEquals(secret, new AppSecret(secret2.getUniqSecretName()).getSecretName());
        });
  }
}
