/*
 * Copyright 2022 Google LLC
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

package com.example.livestream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GetChannelEventTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  private static final String LOCATION = "us-central1";
  private static String PROJECT_ID;
  private static final String CHANNEL_ID =
      "my-channel-" + UUID.randomUUID().toString().substring(0, 25);
  private static final String INPUT_ID =
      "my-input-" + UUID.randomUUID().toString().substring(0, 25);
  private static final String EVENT_ID =
      "my-channel-event-" + UUID.randomUUID().toString().substring(0, 25);
  private static String EVENT_NAME;
  private static final String OUTPUT_URI = "gs://my-bucket/my-output-folder/";

  private static PrintStream originalOut;
  private ByteArrayOutputStream bout;

  private static String requireEnvVar(String varName) {
    String varValue = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName));
    return varValue;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Clean up old resources in the test project.
    TestUtils.cleanAllStale(PROJECT_ID, LOCATION);

    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    EVENT_NAME =
        String.format(
            "projects/%s/locations/%s/channels/%s/events/%s",
            PROJECT_ID, LOCATION, CHANNEL_ID, EVENT_ID);
    try {
      DeleteChannel.deleteChannel(PROJECT_ID, LOCATION, CHANNEL_ID);
    } catch (NotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
      // Don't worry if the channel doesn't already exist.
    }

    try {
      DeleteInput.deleteInput(PROJECT_ID, LOCATION, INPUT_ID);
    } catch (NotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
      // Don't worry if the input doesn't already exist.
    }

    CreateInput.createInput(PROJECT_ID, LOCATION, INPUT_ID);
    CreateChannel.createChannel(PROJECT_ID, LOCATION, CHANNEL_ID, INPUT_ID, OUTPUT_URI);
    StartChannel.startChannel(PROJECT_ID, LOCATION, CHANNEL_ID);
    CreateChannelEvent.createChannelEvent(PROJECT_ID, LOCATION, CHANNEL_ID, EVENT_ID);

    bout.reset();
  }

  @Test
  public void test_GetChannelEvent() throws Exception {
    GetChannelEvent.getChannelEvent(PROJECT_ID, LOCATION, CHANNEL_ID, EVENT_ID);
    String output = bout.toString();
    assertThat(output, containsString(EVENT_NAME));
    bout.reset();
  }

  @After
  public void tearDown()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    StopChannel.stopChannel(PROJECT_ID, LOCATION, CHANNEL_ID);
    DeleteChannelEvent.deleteChannelEvent(PROJECT_ID, LOCATION, CHANNEL_ID, EVENT_ID);
    try {
      DeleteChannel.deleteChannel(PROJECT_ID, LOCATION, CHANNEL_ID);
    } catch (NotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
      System.out.printf(String.valueOf(e));
    }

    try {
      DeleteInput.deleteInput(PROJECT_ID, LOCATION, INPUT_ID);
    } catch (NotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
      System.out.printf(String.valueOf(e));
    }
    System.setOut(originalOut);
    bout.reset();
  }
}
