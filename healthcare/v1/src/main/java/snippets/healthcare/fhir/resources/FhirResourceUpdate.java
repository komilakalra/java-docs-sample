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

package snippets.healthcare.fhir.resources;

// [START healthcare_update_resource]

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class FhirResourceUpdate {
  private static final String FHIR_NAME =
      "projects/%s/locations/%s/datasets/%s/fhirStores/%s/fhir/%s/%s";
  private static final JsonFactory JSON_FACTORY = new GsonFactory();
  private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void fhirResourceUpdate(String resourceName, String data)
      throws IOException, URISyntaxException {
    // String resourceName =
    //    String.format(
    //        FHIR_NAME, "project-id", "region-id", "dataset-id", "store-id", "resource-type",
    // "resource-id");
    // The following data works with a Patient resource and is not intended to work with
    // other types of FHIR resources. If necessary, supply new values for data that
    // correspond to the FHIR resource you are patching.
    // String data = "[{\"op\": \"replace\", \"path\": \"/active\", \"value\": false}]";

    // Initialize the client, which will be used to interact with the service.
    CloudHealthcare client = createClient();

    HttpClient httpClient = HttpClients.createDefault();
    String uri = String.format("%sv1/%s", client.getRootUrl(), resourceName);
    URIBuilder uriBuilder = new URIBuilder(uri).setParameter("access_token", getAccessToken());
    StringEntity requestEntity = new StringEntity(data);

    HttpUriRequest request =
        RequestBuilder.put(uriBuilder.build())
            .setEntity(requestEntity)
            .addHeader("Content-Type", "application/fhir+json;charset=utf-8")
            .addHeader("Accept-Charset", "utf-8")
            .addHeader("Accept", "application/fhir+json; charset=utf-8")
            .build();

    // Execute the request and process the results.
    HttpResponse response = httpClient.execute(request);
    HttpEntity responseEntity = response.getEntity();
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      System.err.print(
          String.format(
              "Exception updating FHIR resource: %s\n", response.getStatusLine().toString()));
      responseEntity.writeTo(System.err);
      throw new RuntimeException();
    }
    System.out.println("FHIR resource updated: ");
    responseEntity.writeTo(System.out);
  }

  private static CloudHealthcare createClient() throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information see https://cloud.google.com/docs/authentication/production
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    // Create a HttpRequestInitializer, which will provide a baseline configuration to all requests.
    HttpRequestInitializer requestInitializer =
        request -> {
          new HttpCredentialsAdapter(credential).initialize(request);
          request.setConnectTimeout(60000); // 1 minute connect timeout
          request.setReadTimeout(60000); // 1 minute read timeout
        };

    // Build the client for interacting with the service.
    return new CloudHealthcare.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
        .setApplicationName("your-application-name")
        .build();
  }

  private static String getAccessToken() throws IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));
    
    return credential.refreshAccessToken().getTokenValue();
  }
}
// [END healthcare_update_resource]
