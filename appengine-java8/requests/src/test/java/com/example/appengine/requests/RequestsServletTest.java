/*
 * Copyright 2016 Google Inc.
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

package com.example.appengine.requests;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link RequestsServlet}.
 */
@RunWith(JUnit4.class)
public class RequestsServletTest {

  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private RequestsServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new RequestsServlet();
  }

  @Test
  public void doGet_writesResponse() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);

    // We expect a greeting to be returned.
    assertWithMessage("RequestsServlet response")
        .that(responseWriter.toString())
        .contains("Hello, world");
  }
}
