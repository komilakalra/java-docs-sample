/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static com.google.common.truth.Truth.assertThat;

import com.example.jobs.JobSearchCreateTenant;
import com.example.jobs.JobSearchDeleteTenant;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class JobSearchCreateTenantTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String TENANT_EXT_ID =
      String.format("EXTERNAL_TEMP_TENANT_ID_%s", UUID.randomUUID().toString().substring(0, 20));

  private String tenantId;
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void testCreateTenant() throws IOException {
    // create a tenant.
    JobSearchCreateTenant.createTenant(PROJECT_ID, TENANT_EXT_ID);

    String got = bout.toString();
    assertThat(got).contains("Created Tenant");

    tenantId = JobSearchGetJobTest.extractLastId(got.split("\n")[1]);
  }

  @After
  public void tearDown() throws IOException {

    // clean up.
    JobSearchDeleteTenant.deleteTenant(PROJECT_ID, tenantId);
    System.setOut(null);
  }
}
