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

package dlp.snippets;

// [START dlp_inspect_augment_infotypes]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ByteContentItem;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CustomInfoType;
import com.google.privacy.dlp.v2.Finding;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectContentRequest;
import com.google.privacy.dlp.v2.InspectContentResponse;
import com.google.privacy.dlp.v2.LocationName;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InspectStringAugmentInfoType {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The string to de-identify.
    String textToInspect = "The patient's name is quasimodo";
    // The string to be additionally matched.
    List<String> wordList = Arrays.asList("quasimodo");
    inspectStringAugmentInfoType(projectId, textToInspect, wordList);
  }

  // Inspects the text using new custom words added to the dictionary.
  public static void inspectStringAugmentInfoType(
      String projectId, String textToInspect, List<String> wordList) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify the type and content to be inspected.
      ByteContentItem byteItem =
          ByteContentItem.newBuilder()
              .setType(ByteContentItem.BytesType.TEXT_UTF8)
              .setData(ByteString.copyFromUtf8(textToInspect))
              .build();
      ContentItem item = ContentItem.newBuilder().setByteItem(byteItem).build();

      // Construct the custom word list to be detected.
      CustomInfoType.Dictionary dictionary =
          CustomInfoType.Dictionary.newBuilder()
              .setWordList(
                  CustomInfoType.Dictionary.WordList.newBuilder().addAllWords(wordList).build())
              .build();

      InfoType infoType = InfoType.newBuilder().setName("PERSON_NAME").build();
      // Construct a custom infotype detector by augmenting the PERSON_NAME detector with a word
      // list.
      CustomInfoType customInfoType =
          CustomInfoType.newBuilder().setInfoType(infoType).setDictionary(dictionary).build();

      InspectConfig inspectConfig =
          InspectConfig.newBuilder()
              .addCustomInfoTypes(customInfoType)
              .setIncludeQuote(true)
              .build();

      // Construct the Inspect request to be sent by the client.
      InspectContentRequest request =
          InspectContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(item)
              .setInspectConfig(inspectConfig)
              .build();

      // Use the client to send the API request.
      InspectContentResponse response = dlp.inspectContent(request);

      // Parse the response and process results
      System.out.println("Findings: " + response.getResult().getFindingsCount());
      for (Finding f : response.getResult().getFindingsList()) {
        System.out.println("\tQuote: " + f.getQuote());
        System.out.println("\tInfo type: " + f.getInfoType().getName());
        System.out.println("\tLikelihood: " + f.getLikelihood());
      }
    }
  }
}

// [END dlp_inspect_augment_infotypes]
