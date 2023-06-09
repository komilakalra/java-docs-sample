/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.monitoring;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for delete alert policy sample. */
@RunWith(JUnit4.class)
public class DeleteAlertPolicyIT {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  private static final String PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");
  private static final String suffix = UUID.randomUUID().toString().substring(0, 8);
  private ByteArrayOutputStream bout;
  private String alertPolicyId;
  private String alertPolicyDisplayName;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
    return value;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() throws IOException {
    alertPolicyDisplayName = "alert_policy_name_" + suffix;
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    // create an alert policy
    CreateAlertPolicy.createAlertPolicy(PROJECT_ID, alertPolicyDisplayName);
    String result = bout.toString();
    alertPolicyId = result.substring(result.indexOf(":") + 1);
    bout.reset();
    out.flush();
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void deleteAlertPolicyTest() throws IOException {
    DeleteAlertPolicy.deleteAlertPolicy(alertPolicyId);
    assertThat(bout.toString()).contains("alert policy deleted successfully");
  }
}
