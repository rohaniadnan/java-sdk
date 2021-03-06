/*
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.watson.assistant.v1.tone_analyzer_integration;

import java.util.HashMap;
import java.util.Map;

import com.ibm.watson.assistant.v1.Assistant;
import com.ibm.watson.assistant.v1.model.Context;
import com.ibm.watson.assistant.v1.model.InputData;
import com.ibm.watson.assistant.v1.model.MessageOptions;
import com.ibm.watson.assistant.v1.model.MessageResponse;
import com.ibm.watson.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.tone_analyzer.v3.model.ToneOptions;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCallback;
import com.ibm.cloud.sdk.core.service.security.IamOptions;


/**
 * Example of how to integrate the Watson Assistant and Tone Analyzer services.
 */
public class AssistantToneAnalyzerIntegrationExample {

  public static void main(String[] args) throws Exception {

    // instantiate the assistant service
    Assistant assistantService = new Assistant("2018-02-16");
    IamOptions options = new IamOptions.Builder()
        .apiKey("<iam_api_key>")
        .build();
    assistantService.setIamCredentials(options);

    // instantiate the tone analyzer service
    ToneAnalyzer toneService = new ToneAnalyzer("2017-09-21");
    IamOptions options = new IamOptions.Builder()
        .apiKey("<iam_api_key>")
        .build();
    toneService.setIamCredentials(options);

    // workspace id
    String workspaceId = "<workspace-id>";

    // maintain history in the context variable - will add a history variable to
    // each of the emotion, social
    // and language tones
    boolean maintainHistory = false;

    /**
     * Input for the Assistant service: input (String): an input string (the user's conversation turn) and context
     * (Map<String,Object>: any context that needs to be maintained - either added by the client app or passed in the
     * response from the Assistant service on the previous conversation turn.
     */
    String input = "I am happy";
    Map<String, Object> context = new HashMap<>();

    // UPDATE CONTEXT HERE IF CONTINUING AN ONGOING CONVERSATION
    // set local context variable to the context from the last response from the
    // Assistant Service
    // (see the getContext() method of the MessageResponse class in
    // com.ibm.watson.developer_cloud.assistant.v1.model)

    // async call to Tone Analyzer
    ToneOptions toneOptions = new ToneOptions.Builder()
        .text(input)
        .build();
    toneService.tone(toneOptions).enqueue(new ServiceCallback<ToneAnalysis>() {
      @Override
      public void onResponse(Response<ToneAnalysis> toneResponsePayload) {

        // update context with the tone data returned by the Tone Analyzer
        context = ToneDetection.updateUserTone(context, toneResponsePayload.getResult(), maintainHistory);

        // call Assistant Service with the input and tone-aware context
        MessageOptions messageOptions = new MessageOptions.Builder(workspaceId)
            .input(new InputData.Builder(input).build())
            .context((Context) context)
            .build();
        assistantService.message(messageOptions).enqueue(new ServiceCallback<MessageResponse>() {
          @Override
          public void onResponse(Response<MessageResponse> response) {
            System.out.println(response.getResult());
          }

          @Override
          public void onFailure(Exception e) { }
        });
      }

      @Override
      public void onFailure(Exception e) { }
    });
  }
}
