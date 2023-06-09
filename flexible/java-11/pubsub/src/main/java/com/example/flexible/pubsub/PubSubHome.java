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

package com.example.flexible.pubsub;

import java.util.List;

public class PubSubHome {

  private static MessageRepository messageRepository = MessageRepositoryImpl.getInstance();
  private static int MAX_MESSAGES = 10;

  /**
   * Retrieve received messages in html.
   *
   * @return html representation of messages (one per row)
   */
  public static String getReceivedMessages() {
    List<Message> messageList = messageRepository.retrieve(MAX_MESSAGES);
    System.out.println(messageList.size());
    return convertToHtmlTable(messageList);
  }

  private static String convertToHtmlTable(List<Message> messages) {
    StringBuilder sb = new StringBuilder();
    for (Message message : messages) {
      sb.append("<tr>");
      sb.append("<td>" + message.getMessageId() + "</td>");
      sb.append("<td>" + message.getData() + "</td>");
      sb.append("<td>" + message.getPublishTime() + "</td>");
      sb.append("</tr>");
    }
    return sb.toString();
  }

  private PubSubHome() { }
}
