/*
 * Copyright 2023 Google LLC
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

package com.example.appengine;

import static org.junit.Assert.assertEquals;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HelloControllerTest {
  private static EmbeddedServer server;
  private static HttpClient client;

  @BeforeClass
  public static void setupServer() {

    server = ApplicationContext.run(EmbeddedServer.class);

    client = server.getApplicationContext().createBean(HttpClient.class, server.getURL());
  }

  @AfterClass
  public static void stopServer() {
    if (client != null) {
      client.stop();
    }
    if (server != null) {
      server.stop();
    }
  }

  @Test
  public void testHelloWorldResponse() {
    String response = client.toBlocking().retrieve(HttpRequest.GET("/"));
    assertEquals("Hello World!", response);
  }
}
