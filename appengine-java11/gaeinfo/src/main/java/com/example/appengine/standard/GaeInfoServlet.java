/*
 * Copyright 2019 Google LLC
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

package com.example.appengine.standard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

@SuppressWarnings({"serial"})
@WebServlet(
    name = "GAEInfo",
    description = "GAEInfo: Write info about GAE Standard",
    urlPatterns = "/")
public class GaeInfoServlet extends HttpServlet {

  private final String[] metaPath = {
    "/computeMetadata/v1/project/numeric-project-id",
    "/computeMetadata/v1/project/project-id",
    "/computeMetadata/v1/instance/zone",
    "/computeMetadata/v1/instance/service-accounts/default/aliases",
    "/computeMetadata/v1/instance/service-accounts/default/",
    "/computeMetadata/v1/instance/service-accounts/default/scopes",
    // Tokens work - but are a security risk to display
    // "/computeMetadata/v1/instance/service-accounts/default/token"
  };

  final String[] metaServiceAcct = {
    "/computeMetadata/v1/instance/service-accounts/{account}/aliases",
    "/computeMetadata/v1/instance/service-accounts/{account}/email",
    "/computeMetadata/v1/instance/service-accounts/{account}/scopes",
    // Tokens work - but are a security risk to display
    // "/computeMetadata/v1/instance/service-accounts/{account}/token"
  };

  private final String metadata = "http://metadata.google.internal";

  private TemplateEngine templateEngine;
  private JavaxServletWebApplication application;

  // Use OkHttp from Square as it's quite easy to use for simple fetches.
  private final OkHttpClient ok =
      new OkHttpClient.Builder()
          .readTimeout(500, TimeUnit.MILLISECONDS) // Don't dawdle
          .writeTimeout(500, TimeUnit.MILLISECONDS)
          .build();

  // Setup to pretty print returned json
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  // Fetch Metadata
  String fetchMetadata(String key) throws IOException {
    Request request =
        new Request.Builder()
            .url(metadata + key)
            .addHeader("Metadata-Flavor", "Google")
            .get()
            .build();

    Response response = ok.newCall(request).execute();
    return response.body().string();
  }

  String fetchJsonMetadata(String prefix) throws IOException {
    Request request =
        new Request.Builder()
            .url(metadata + prefix)
            .addHeader("Metadata-Flavor", "Google")
            .get()
            .build();

    Response response = ok.newCall(request).execute();

    // Convert json to pretty json
    return gson.toJson(JsonParser.parseString(response.body().string()));
  }

  @Override
  public void init() {
    // Setup ThymeLeaf
    application = JavaxServletWebApplication.buildApplication(this.getServletContext());
    WebApplicationTemplateResolver templateResolver =
        new WebApplicationTemplateResolver(application);

    templateResolver.setPrefix("/WEB-INF/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCacheTTLMs(1200000L); // TTL=20m

    // Cache is set to true by default. Set to false if you want templates to
    // be automatically updated when modified.
    templateResolver.setCacheable(true);

    templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String key;
    WebContext ctx = new WebContext(application.buildExchange(req, resp));
    ctx.setLocale(req.getLocale());

    resp.setContentType("text/html");

    TreeMap<String, String> m = new TreeMap<>();

    for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements(); ) {
      key = e.nextElement();
      m.put(key, req.getHeader(key));
    }
    ctx.setVariable("headers", m);

    Cookie[] cookies = req.getCookies();
    m = new TreeMap<>();
    if (cookies != null && cookies.length != 0) {
      for (Cookie co : cookies) {
        m.put(co.getName(), co.getValue());
      }
    }
    ctx.setVariable("cookies", m);

    Properties properties = System.getProperties();
    m = new TreeMap<>();
    for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
      key = (String) e.nextElement();
      m.put(key, (String) properties.get(key));
    }
    ctx.setVariable("systemprops", m);

    Map<String, String> envVar = System.getenv();
    m = new TreeMap<>(envVar);
    ctx.setVariable("envvar", m);

    // The metadata server is only on a production system
    m = new TreeMap<>();
    for (String k : metaPath) {
      m.put(k, fetchMetadata(k));
    }
    ctx.setVariable("Metadata", m.descendingMap());

    ctx.setVariable("sam", m.descendingMap());

    // Recursively get all info about service accounts -- Note tokens are leftout by default.
    ctx.setVariable(
        "rsa", fetchJsonMetadata("/computeMetadata/v1/instance/service-accounts/?recursive=true"));
    // Recursively get all data on Metadata server.
    ctx.setVariable("ram", fetchJsonMetadata("/?recursive=true"));

    templateEngine.process("index", ctx, resp.getWriter());
  }
}
