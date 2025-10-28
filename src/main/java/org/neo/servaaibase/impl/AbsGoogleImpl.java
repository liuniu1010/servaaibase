package org.neo.servaaibase.impl;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Base64;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.neo.servaframe.util.IOUtil;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

abstract public class AbsGoogleImpl implements SuperAIIFC {
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AbsGoogleImpl.class);

    abstract protected String getApiKey();
    abstract protected String getUrl(String model);
    abstract protected int getMaxOutputTokenNumber(String model);
    abstract protected int getContextWindow(String model);
    abstract protected String getDefaultSystemHint();
    abstract protected boolean isSupportSystemHint(String model);

    @Override
    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct) {
        try {
            return elasticFetchChatResponse(model, promptStruct);
        }
        catch(NeoAIException nex) {
            logger.error(nex.getMessage(), nex);
            throw nex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new NeoAIException(ex);
        }
    }

    @Override
    public AIModel.Embedding getEmbedding(String model, String input) {
        try {
            return elasticGetEmbedding(model, input, -1);
        }
        catch(NeoAIException nex) {
            logger.error(nex.getMessage(), nex);
            throw nex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new NeoAIException(ex);
        }
    }

    @Override
    public AIModel.Embedding getEmbedding(String model, String input, int dimensions) {
        try {
            return elasticGetEmbedding(model, input, dimensions);
        }
        catch(NeoAIException nex) {
            logger.error(nex.getMessage(), nex);
            throw nex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new NeoAIException(ex);
        }
    }

    @Override
    public String[] generateImages(String model, AIModel.ImagePrompt imagePrompt) {
        try {
            return elasticGenerateImage(model, imagePrompt);
        }
        catch(NeoAIException nex) {
            logger.error(nex.getMessage(), nex);
            throw nex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new NeoAIException(ex);
        }
    }

    @Override
    public String textToSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileAbsolutePath) {
        try {
            return elasticTextToSpeech(model, textToSpeechPrompt, onlineFileAbsolutePath);
        }
        catch(NeoAIException nex) {
            logger.error(nex.getMessage(), nex);
            throw nex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new NeoAIException(ex);
        }
    }

    @Override
    public AIModel.ChatResponse speechToText(String model, AIModel.Attachment attachment) {
        try {
            return elasticSpeechToText(model, attachment);
        }
        catch(NeoAIException nex) {
            logger.error(nex.getMessage(), nex);
            throw nex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new NeoAIException(ex);
        }
    }

    private AIModel.ChatResponse elasticFetchChatResponse(String model, AIModel.PromptStruct promptStruct) throws Exception {
        int retryTimesOnLLMException = CommonUtil.getConfigValueAsInt("retryTimesOnLLMException");
        int waitSeconds = CommonUtil.getConfigValueAsInt("firstWaitSecondsOnLLMException");
        for(int i = 0;i < retryTimesOnLLMException;i++) {
            try {
                return innerFetchChatResponse(model, promptStruct);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY) {
                    try {
                        logger.info("Meet IOException or syntax exception from LLM, wait " + waitSeconds + " seconds and try again...");
                        Thread.sleep(1000 * waitSeconds);
                        waitSeconds = waitSeconds * 2;
                    }
                    catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct) throws Exception {
        int maxTokens = determineMaxTokens(model, promptStruct);
        AIModel.ChatResponse chatResponse = innerFetchChatResponse(model, promptStruct, maxTokens);
        return chatResponse;
    }

    private String[] elasticGenerateImage(String model, AIModel.ImagePrompt imagePrompt) throws Exception {
        int retryTimesOnLLMException = CommonUtil.getConfigValueAsInt("retryTimesOnLLMException");
        int waitSeconds = CommonUtil.getConfigValueAsInt("firstWaitSecondsOnLLMException");
        for(int i = 0;i < retryTimesOnLLMException;i++) {
            try {
                return innerGenerateImage(model, imagePrompt);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY) {
                    try {
                        logger.info("Meet IOException or syntax exception from LLM, wait " + waitSeconds + " seconds and try again...");
                        Thread.sleep(1000 * waitSeconds);
                        waitSeconds = waitSeconds * 2;
                    }
                    catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
    }

    private String[] innerGenerateImage(String model, AIModel.ImagePrompt imagePrompt) throws Exception {
        String jsonInput = generateJsonBodyToGenerateImage(model, imagePrompt);
        String jsonResponse = sendPost(model, jsonInput);
        String[] urls = extractImageUrlsFromJson(jsonResponse);
        return urls;
    }

    private String elasticTextToSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileAbsolutePath) throws Exception {
        int retryTimesOnLLMException = CommonUtil.getConfigValueAsInt("retryTimesOnLLMException");
        int waitSeconds = CommonUtil.getConfigValueAsInt("firstWaitSecondsOnLLMException");
        for(int i = 0;i < retryTimesOnLLMException;i++) {
            try {
                return innerTextToSpeech(model, textToSpeechPrompt, onlineFileAbsolutePath);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY) {
                    try {
                        logger.info("Meet IOException or syntax exception from LLM, wait " + waitSeconds + " seconds and try again...");
                        Thread.sleep(1000 * waitSeconds);
                        waitSeconds = waitSeconds * 2;
                    }
                    catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
    }

    private String innerTextToSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileAbsolutePath) throws Exception {
        String jsonInput = generateJsonBodyToTextToSpeech(model, textToSpeechPrompt);
        String fileName = "audio_" + CommonUtil.getRandomString(10) + "." + textToSpeechPrompt.getOutputFormat();
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;
        sendPostAndGenerateFile(model, jsonInput, filePath);
        return fileName;
    }

    private AIModel.ChatResponse elasticSpeechToText(String model, AIModel.Attachment attachment) throws Exception {
        int retryTimesOnLLMException = CommonUtil.getConfigValueAsInt("retryTimesOnLLMException");
        int waitSeconds = CommonUtil.getConfigValueAsInt("firstWaitSecondsOnLLMException");
        for(int i = 0;i < retryTimesOnLLMException;i++) {
            try {
                return innerSpeechToText(model, attachment);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY) {
                    try {
                        logger.info("Meet IOException or syntax exception from LLM, wait " + waitSeconds + " seconds and try again...");
                        Thread.sleep(1000 * waitSeconds);
                        waitSeconds = waitSeconds * 2;
                    }
                    catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
    }

    private AIModel.ChatResponse innerSpeechToText(String model, AIModel.Attachment attachment) throws Exception {
        String filePath = attachment.getContent();
        String jsonResponse = sendPostWithFormData(model, filePath);
        AIModel.ChatResponse chatResponse = extractTextFromSpeechJson(jsonResponse);
        return chatResponse;
    }

    private AIModel.Embedding elasticGetEmbedding(String model, String input, int dimensions) throws Exception {
        int retryTimesOnLLMException = CommonUtil.getConfigValueAsInt("retryTimesOnLLMException");
        int waitSeconds = CommonUtil.getConfigValueAsInt("firstWaitSecondsOnLLMException");
        for(int i = 0;i < retryTimesOnLLMException;i++) {
            try {
                return innerGetEmbedding(model, input, dimensions);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY) {
                    try {
                        logger.info("Meet IOException or syntax exception from LLM, wait " + waitSeconds + " seconds and try again...");
                        Thread.sleep(1000 * waitSeconds);
                        waitSeconds = waitSeconds * 2;
                    }
                    catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
    }

    private AIModel.Embedding innerGetEmbedding(String model, String input, int dimensions) throws Exception {
        String jsonInput = generateJsonBodyToGetEmbedding(model, input, dimensions);
        String jsonResponse = sendPost(model, jsonInput);
        AIModel.TokensUsage tokensUsage = extractTokensUsageFromJson(jsonResponse);
        AIModel.Embedding embedding = extractEmbeddingFromJson(jsonResponse);
        embedding.setTokensUsage(tokensUsage);
        return embedding;
    }

    private int determineMaxTokens(String model, AIModel.PromptStruct promptStruct) throws Exception {
        boolean needCalculate = false; // calculate prompt token number or not
        if(needCalculate) { // in this way, calculate prompt token number first
            int promptTokenNumber = fetchPromptTokenNumber(model, promptStruct);
            if(promptTokenNumber < 0) {
                throw new NeoAIException("some error occurred for promptTokenNumber < 0");
            }

            return Math.min(getMaxOutputTokenNumber(model), (getContextWindow(model) - promptTokenNumber));
        }
        else {
            return getMaxOutputTokenNumber(model); // in this way, don't calcuate prompt token number
        }
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens) throws Exception {
        String jsonInput = generateJsonBodyToFetchResponse(model, promptStruct, maxTokens);
        String jsonResponse = sendPost(model, jsonInput);
        List<AIModel.Call> calls = extractCallsFromJson(jsonResponse);
        AIModel.TokensUsage tokensUsage = extractTokensUsageFromJson(jsonResponse);
        AIModel.ChatResponse chatResponse = extractChatResponseFromJson(jsonResponse);
        chatResponse.setCalls(calls);
        chatResponse.setTokensUsage(tokensUsage);
        return chatResponse;
    }

    private int fetchPromptTokenNumber(String model, AIModel.PromptStruct promptStruct) throws Exception {
        String jsonInput = generateJsonBodyForGetTokenNumber(model, promptStruct);
        String jsonTokenNumber = sendPostToUrl(buildCountTokensUrl(model), jsonInput, model);
        int tokenNumber = extractTokenNumberFromJson(jsonTokenNumber);
        return tokenNumber;
    }

    private int extractTokenNumberFromJson(String jsonTokenNumber) {
        try {
            JsonElement element = JsonParser.parseString(jsonTokenNumber);
            if (!element.isJsonObject()) {
                throw new NeoAIException("Token response is not a JSON object");
            }
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("totalTokens") && !jsonObject.get("totalTokens").isJsonNull()) {
                return jsonObject.get("totalTokens").getAsInt();
            }
            if (jsonObject.has("usageMetadata") && jsonObject.get("usageMetadata").isJsonObject()) {
                JsonObject usage = jsonObject.getAsJsonObject("usageMetadata");
                if (usage.has("totalTokenCount") && !usage.get("totalTokenCount").isJsonNull()) {
                    return usage.get("totalTokenCount").getAsInt();
                }
            }
            throw new NeoAIException("Token response missing totalTokens");
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting prompt token number", ex);
        }
    }

    private AIModel.Embedding extractEmbeddingFromJson(String jsonResponse) {
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            if (!element.isJsonObject()) {
                throw new NeoAIException("Embedding response is not a JSON object");
            }
            JsonObject jsonObject = element.getAsJsonObject();

            JsonObject embeddingObject = null;
            if (jsonObject.has("embedding") && jsonObject.get("embedding").isJsonObject()) {
                embeddingObject = jsonObject.getAsJsonObject("embedding");
            }
            else if (jsonObject.has("embeddings") && jsonObject.get("embeddings").isJsonArray()) {
                JsonArray embeddingsArray = jsonObject.getAsJsonArray("embeddings");
                if (embeddingsArray.size() > 0 && embeddingsArray.get(0).isJsonObject()) {
                    embeddingObject = embeddingsArray.get(0).getAsJsonObject();
                }
            }

            if (embeddingObject == null) {
                throw new NeoAIException("Embedding response has no embedding object");
            }

            JsonArray values = embeddingObject.has("values") && embeddingObject.get("values").isJsonArray()
                    ? embeddingObject.getAsJsonArray("values")
                    : null;
            if (values == null) {
                throw new NeoAIException("Embedding values missing in response");
            }

            double[] data = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                data[i] = values.get(i).getAsDouble();
            }

            return new AIModel.Embedding(data);
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting embedding", ex);
        }
    }

    private String[] extractImageUrlsFromJson(String jsonResponse) {
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            if (!element.isJsonObject()) {
                throw new NeoAIException("Image response is not a JSON object");
            }
            JsonObject jsonObject = element.getAsJsonObject();

            List<String> collected = new ArrayList<String>();

            JsonArray predictionsArray = jsonObject.has("predictions") && jsonObject.get("predictions").isJsonArray()
                    ? jsonObject.getAsJsonArray("predictions")
                    : null;
            if (predictionsArray != null) {
                for (int i = 0; i < predictionsArray.size(); i++) {
                    JsonObject predictionObject = predictionsArray.get(i).isJsonObject()
                            ? predictionsArray.get(i).getAsJsonObject()
                            : null;
                    collectImagePayloadFromPrediction(predictionObject, collected);
                }
            }

            if (collected.isEmpty()) {
                JsonArray imagesArray = null;
                if (jsonObject.has("images") && jsonObject.get("images").isJsonArray()) {
                    imagesArray = jsonObject.getAsJsonArray("images");
                }
                else if (jsonObject.has("generatedImages") && jsonObject.get("generatedImages").isJsonArray()) {
                    imagesArray = jsonObject.getAsJsonArray("generatedImages");
                }

                if (imagesArray != null) {
                    for (int i = 0; i < imagesArray.size(); i++) {
                        JsonObject imageObject = imagesArray.get(i).isJsonObject()
                                ? imagesArray.get(i).getAsJsonObject()
                                : null;
                        if (imageObject == null) {
                            continue;
                        }

                        if (imageObject.has("imageUri") && !imageObject.get("imageUri").isJsonNull()) {
                            collected.add(imageObject.get("imageUri").getAsString());
                            continue;
                        }

                        if (imageObject.has("inlineData") && imageObject.get("inlineData").isJsonObject()) {
                            JsonObject inlineData = imageObject.getAsJsonObject("inlineData");
                            collectInlineImageData(inlineData, "image/png", collected);
                        }
                    }
                }
            }

            if (collected.isEmpty()) {
                throw new NeoAIException("Image response contains no usable image payload");
            }

            return collected.toArray(new String[collected.size()]);
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting image urls", ex);
        }
    }

    private AIModel.TokensUsage extractTokensUsageFromJson(String jsonResponse) throws Exception {
        AIModel.TokensUsage tokensUsage = new AIModel.TokensUsage();
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            if (!element.isJsonObject()) {
                return tokensUsage;
            }
            JsonObject jsonObject = element.getAsJsonObject();
            if (!jsonObject.has("usageMetadata") || !jsonObject.get("usageMetadata").isJsonObject()) {
                return tokensUsage;
            }

            JsonObject usage = jsonObject.getAsJsonObject("usageMetadata");
            if (usage.has("promptTokenCount") && !usage.get("promptTokenCount").isJsonNull()) {
                tokensUsage.setInputTokens(usage.get("promptTokenCount").getAsInt());
            }
            if (usage.has("candidatesTokenCount") && !usage.get("candidatesTokenCount").isJsonNull()) {
                tokensUsage.setOutputTokens(usage.get("candidatesTokenCount").getAsInt());
            }
            if (usage.has("cachedContentTokenCount") && !usage.get("cachedContentTokenCount").isJsonNull()) {
                tokensUsage.setCachedTokens(usage.get("cachedContentTokenCount").getAsInt());
            }
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting token usage", ex);
        }

        return tokensUsage;
    }

    private List<AIModel.Call> extractCallsFromJson(String jsonResponse) throws Exception {
        List<AIModel.Call> calls = new ArrayList<AIModel.Call>();
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            if (!element.isJsonObject()) {
                return null;
            }
            JsonObject jsonObject = element.getAsJsonObject();
            if (!jsonObject.has("candidates") || !jsonObject.get("candidates").isJsonArray()) {
                return null;
            }

            JsonArray candidates = jsonObject.getAsJsonArray("candidates");
            for (int i = 0; i < candidates.size(); i++) {
                JsonObject candidate = candidates.get(i).isJsonObject() ? candidates.get(i).getAsJsonObject() : null;
                if (candidate == null || !candidate.has("content") || !candidate.get("content").isJsonObject()) {
                    continue;
                }

                JsonObject content = candidate.getAsJsonObject("content");
                if (!content.has("parts") || !content.get("parts").isJsonArray()) {
                    continue;
                }

                JsonArray parts = content.getAsJsonArray("parts");
                for (int j = 0; j < parts.size(); j++) {
                    JsonObject part = parts.get(j).isJsonObject() ? parts.get(j).getAsJsonObject() : null;
                    if (part == null || !part.has("functionCall") || !part.get("functionCall").isJsonObject()) {
                        continue;
                    }

                    JsonObject functionCall = part.getAsJsonObject("functionCall");
                    if (!functionCall.has("name") || functionCall.get("name").isJsonNull()) {
                        continue;
                    }

                    String methodName = functionCall.get("name").getAsString();
                    AIModel.Call call = new AIModel.Call();
                    call.setMethodName(methodName);

                    String argumentsString = "";
                    if (functionCall.has("args") && functionCall.get("args").isJsonObject()) {
                        argumentsString = functionCall.getAsJsonObject("args").toString();
                    }
                    else if (functionCall.has("arguments")) {
                        JsonElement argumentsElement = functionCall.get("arguments");
                        argumentsString = argumentsElement == null || argumentsElement.isJsonNull()
                                ? ""
                                : (argumentsElement.isJsonPrimitive() && argumentsElement.getAsJsonPrimitive().isString()
                                    ? argumentsElement.getAsString()
                                    : argumentsElement.toString());
                    }

                    List<AIModel.CallParam> callParams = extractArgumentsString(argumentsString);
                    call.setParams(callParams);
                    calls.add(call);
                }
            }
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting tool calls", ex);
        }

        return calls.isEmpty() ? null : calls;
    }

    private boolean isJsonString(String argumentsString) {
        if (argumentsString == null || argumentsString.isEmpty()) {
            return false;
        }
        try {
            JsonParser.parseString(argumentsString);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    // handle possible malformed function call payloads from upstream provider
    private List<AIModel.CallParam> extractArgumentsString(String argumentsString) {
        List<AIModel.CallParam> callParams = new ArrayList<AIModel.CallParam>();
        if (argumentsString == null) {
            return callParams;
        }

        if (isJsonString(argumentsString)) { // by design it should be a json string, sometimes it return a normal string
            try {
                JsonElement elementArguments = JsonParser.parseString(argumentsString);
                if (elementArguments.isJsonObject()) {
                    JsonObject argumentsObject = elementArguments.getAsJsonObject();
                    Set<String> paramNames = argumentsObject.keySet();
                    for (String paramName : paramNames) {
                        AIModel.CallParam callParam = new AIModel.CallParam();
                        callParam.setName(paramName);
                        JsonElement valueElement = argumentsObject.get(paramName);
                        callParam.setValue(valueElement == null || valueElement.isJsonNull() ? null : valueElement.toString());
                        callParams.add(callParam);
                    }
                }
                else if (elementArguments.isJsonArray()) {
                    AIModel.CallParam callParam = new AIModel.CallParam();
                    callParam.setName(AIModel.CallParam.UNKNOWN);
                    callParam.setValue(elementArguments.toString());
                    callParams.add(callParam);
                }
            }
            catch (JsonSyntaxException ex) {
                // fall back to treating the payload as raw text
                AIModel.CallParam callParam = new AIModel.CallParam();
                callParam.setName(AIModel.CallParam.UNKNOWN);
                callParam.setValue(argumentsString);
                callParams.add(callParam);
            }
        }
        else {
            AIModel.CallParam callParam = new AIModel.CallParam();
            callParam.setName(AIModel.CallParam.UNKNOWN);
            callParam.setValue(argumentsString);
            callParams.add(callParam);
        }

        return callParams;
    }

    private AIModel.ChatResponse extractTextFromSpeechJson(String jsonResponse) throws Exception {
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            if (!element.isJsonObject()) {
                throw new NeoAIException("Speech-to-text response is not a JSON object");
            }
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("results") && jsonObject.get("results").isJsonArray()) {
                JsonArray results = jsonObject.getAsJsonArray("results");
                StringBuilder transcriptBuilder = new StringBuilder();
                for (int i = 0; i < results.size(); i++) {
                    JsonObject result = results.get(i).isJsonObject() ? results.get(i).getAsJsonObject() : null;
                    if (result == null || !result.has("alternatives") || !result.get("alternatives").isJsonArray()) {
                        continue;
                    }

                    JsonArray alternatives = result.getAsJsonArray("alternatives");
                    if (alternatives.size() == 0) {
                        continue;
                    }

                    JsonObject alternative = alternatives.get(0).isJsonObject() ? alternatives.get(0).getAsJsonObject() : null;
                    if (alternative == null || !alternative.has("transcript") || alternative.get("transcript").isJsonNull()) {
                        continue;
                    }

                    if (transcriptBuilder.length() > 0) {
                        transcriptBuilder.append('\n');
                    }
                    transcriptBuilder.append(alternative.get("transcript").getAsString());
                }

                if (transcriptBuilder.length() > 0) {
                    return new AIModel.ChatResponse(true, transcriptBuilder.toString());
                }
            }
            if (jsonObject.has("error") && jsonObject.get("error").isJsonObject()) {
                JsonObject errorObject = jsonObject.getAsJsonObject("error");
                String errorMessage = errorObject.has("message") && !errorObject.get("message").isJsonNull()
                        ? errorObject.get("message").getAsString()
                        : "Unknown speech-to-text error";
                return new AIModel.ChatResponse(false, errorMessage);
            }
            throw new NeoAIException("Speech-to-text response missing transcript");
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting speech-to-text response", ex);
        }
    }

    private AIModel.ChatResponse extractChatResponseFromJson(String jsonResponse) throws Exception {
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            if (!element.isJsonObject()) {
                throw new NeoAIException("Chat response is not a JSON object");
            }
            JsonObject jsonObject = element.getAsJsonObject();

            if (jsonObject.has("candidates") && jsonObject.get("candidates").isJsonArray()) {
                JsonArray candidates = jsonObject.getAsJsonArray("candidates");
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < candidates.size(); i++) {
                    JsonObject candidate = candidates.get(i).isJsonObject() ? candidates.get(i).getAsJsonObject() : null;
                    if (candidate == null || !candidate.has("content") || !candidate.get("content").isJsonObject()) {
                        continue;
                    }

                    JsonObject content = candidate.getAsJsonObject("content");
                    if (!content.has("parts") || !content.get("parts").isJsonArray()) {
                        continue;
                    }

                    JsonArray parts = content.getAsJsonArray("parts");
                    for (int j = 0; j < parts.size(); j++) {
                        JsonObject part = parts.get(j).isJsonObject() ? parts.get(j).getAsJsonObject() : null;
                        if (part == null) {
                            continue;
                        }
                        if (part.has("text") && !part.get("text").isJsonNull()) {
                            if (builder.length() > 0) {
                                builder.append('\n');
                            }
                            builder.append(part.get("text").getAsString());
                        }
                    }
                }

                return new AIModel.ChatResponse(true, builder.toString());
            }

            if (jsonObject.has("error") && jsonObject.get("error").isJsonObject()) {
                JsonObject errorObject = jsonObject.getAsJsonObject("error");
                String errorMessage = errorObject.has("message") && !errorObject.get("message").isJsonNull()
                        ? errorObject.get("message").getAsString()
                        : "Unknown chat completion error";
                return new AIModel.ChatResponse(false, errorMessage);
            }

            if (jsonObject.has("promptFeedback") && jsonObject.get("promptFeedback").isJsonObject()) {
                JsonObject feedback = jsonObject.getAsJsonObject("promptFeedback");
                if (feedback.has("blockReason") && !feedback.get("blockReason").isJsonNull()) {
                    return new AIModel.ChatResponse(false, feedback.get("blockReason").getAsString());
                }
            }

            throw new NeoAIException("Chat response missing candidates and error payload");
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting chat response", ex);
        }
    }

    private JsonArray generateJsonArrayTools(FunctionCallIFC functionCall) {
        JsonArray tools = new JsonArray();
        if (functionCall == null) {
            return tools;
        }

        List<AIModel.Function> functions = functionCall.getFunctions();
        if (functions == null || functions.isEmpty()) {
            return tools;
        }

        JsonArray functionDeclarations = new JsonArray();
        for (AIModel.Function function : functions) {
            if (function == null) {
                continue;
            }

            String methodName = function.getMethodName();
            if (methodName == null || methodName.isEmpty()) {
                continue;
            }

            JsonObject declaration = new JsonObject();
            declaration.addProperty("name", methodName);
            if (function.getDescription() != null && !function.getDescription().isEmpty()) {
                declaration.addProperty("description", function.getDescription());
            }

            JsonObject parameters = new JsonObject();
            parameters.addProperty("type", "OBJECT");

            JsonObject properties = new JsonObject();
            JsonArray required = new JsonArray();

            List<AIModel.FunctionParam> functionParams = function.getParams();
            if (functionParams != null) {
                for (AIModel.FunctionParam functionParam : functionParams) {
                    if (functionParam == null) {
                        continue;
                    }

                    String paramName = functionParam.getName();
                    if (paramName == null || paramName.isEmpty()) {
                        continue;
                    }

                    JsonObject jsonParam = new JsonObject();
                    jsonParam.addProperty("type", "STRING");
                    if (functionParam.getDescription() != null && !functionParam.getDescription().isEmpty()) {
                        jsonParam.addProperty("description", functionParam.getDescription());
                    }

                    properties.add(paramName, jsonParam);
                    required.add(paramName);
                }
            }

            parameters.add("properties", properties);
            if (required.size() > 0) {
                parameters.add("required", required);
            }

            declaration.add("parameters", parameters);
            functionDeclarations.add(declaration);
        }

        if (functionDeclarations.size() > 0) {
            JsonObject tool = new JsonObject();
            tool.add("functionDeclarations", functionDeclarations);
            tools.add(tool);
        }

        return tools;
    }

    private boolean isVisionModel(String model) {
        String[] visionModels = getVisionModels();
        boolean isVisionModel = false;
        for(String visionModel: visionModels) {
            if(model.equals(visionModel)) {
                return true;
            }
        }
        return false;
    }

    private JsonArray generateJsonArrayMessages(String model, AIModel.PromptStruct promptStruct) {
        JsonArray contents = new JsonArray();

        List<AIModel.ChatRecord> chatRecords = promptStruct != null ? promptStruct.getChatRecords() : null;
        if (chatRecords != null) {
            for (AIModel.ChatRecord chatRecord : chatRecords) {
                if (chatRecord == null) {
                    continue;
                }

                JsonObject content = new JsonObject();
                content.addProperty("role", chatRecord.getIsRequest() ? "user" : "model");

                JsonArray parts = new JsonArray();
                JsonObject textPart = new JsonObject();
                textPart.addProperty("text", chatRecord.getContent() == null ? "" : chatRecord.getContent());
                parts.add(textPart);

                content.add("parts", parts);
                contents.add(content);
            }
        }

        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");

        JsonArray userParts = new JsonArray();
        String userInput = promptStruct != null ? promptStruct.getUserInput() : null;
        JsonObject userTextPart = new JsonObject();
        userTextPart.addProperty("text", userInput == null ? "" : userInput);
        userParts.add(userTextPart);

        if (isVisionModel(model) && promptStruct != null) {
            AIModel.AttachmentGroup attachmentGroup = promptStruct.getAttachmentGroup();
            if (attachmentGroup != null && attachmentGroup.getAttachments() != null) {
                for (AIModel.Attachment attachment : attachmentGroup.getAttachments()) {
                    if (attachment == null || attachment.getContent() == null || attachment.getContent().isEmpty()) {
                        continue;
                    }

                    String attachmentContent = attachment.getContent();
                    if (attachmentContent.startsWith("data:")) {
                        int commaIndex = attachmentContent.indexOf(',');
                        if (commaIndex > 0) {
                            String metadata = attachmentContent.substring(5, commaIndex);
                            String base64Data = attachmentContent.substring(commaIndex + 1);

                            String mimeType = "image/png";
                            int semicolonIndex = metadata.indexOf(';');
                            if (semicolonIndex > 0) {
                                mimeType = metadata.substring(0, semicolonIndex);
                            }

                            JsonObject inlineData = new JsonObject();
                            inlineData.addProperty("mimeType", mimeType);
                            inlineData.addProperty("data", base64Data);

                            JsonObject inlinePart = new JsonObject();
                            inlinePart.add("inlineData", inlineData);
                            userParts.add(inlinePart);
                            continue;
                        }
                    }

                    JsonObject fileData = new JsonObject();
                    fileData.addProperty("fileUri", attachmentContent);

                    JsonObject filePart = new JsonObject();
                    filePart.add("fileData", fileData);
                    userParts.add(filePart);
                }
            }
        }

        userContent.add("parts", userParts);
        contents.add(userContent);

        return contents;
    }

    private String generateJsonBodyForGetTokenNumber(String model, AIModel.PromptStruct promptStruct) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        JsonArray contents = generateJsonArrayMessages(model, promptStruct);
        jsonBody.add("contents", contents);

        JsonObject systemInstruction = generateSystemInstruction(model, promptStruct);
        if (systemInstruction != null) {
            jsonBody.add("systemInstruction", systemInstruction);
        }

        if (promptStruct != null && promptStruct.getFunctionCall() != null) {
            JsonArray tools = generateJsonArrayTools(promptStruct.getFunctionCall());
            if (tools.size() > 0) {
                jsonBody.add("tools", tools);
            }
        }
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToFetchResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        JsonArray contents = generateJsonArrayMessages(model, promptStruct);
        jsonBody.add("contents", contents);

        JsonObject systemInstruction = generateSystemInstruction(model, promptStruct);
        if (systemInstruction != null) {
            jsonBody.add("system_instruction", systemInstruction);
        }

        JsonObject generationConfig = new JsonObject();
        if (maxTokens > 0) {
            generationConfig.addProperty("maxOutputTokens", maxTokens);
        }
        if (generationConfig.size() > 0) {
            jsonBody.add("generationConfig", generationConfig);
        }

        if (promptStruct != null && promptStruct.getFunctionCall() != null) {
            JsonArray tools = generateJsonArrayTools(promptStruct.getFunctionCall());
            if (tools.size() > 0) {
                jsonBody.add("tools", tools);

                JsonObject toolConfig = new JsonObject();
                JsonObject functionCallingConfig = new JsonObject();
                functionCallingConfig.addProperty("mode", "AUTO");
                toolConfig.add("functionCallingConfig", functionCallingConfig);
                jsonBody.add("toolConfig", toolConfig);
            }
        }
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToGetEmbedding(String model, String input, int dimensions) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", input == null ? "" : input);
        parts.add(textPart);
        content.add("parts", parts);
        jsonBody.add("content", content);

        if (dimensions > 0) {
            jsonBody.addProperty("outputDimensionality", dimensions);
        }

        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToGenerateImage(String model, AIModel.ImagePrompt imagePrompt) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        String prompt = imagePrompt != null ? imagePrompt.getUserInput() : null;
        JsonArray instances = new JsonArray();
        JsonObject instance = new JsonObject();
        instance.addProperty("prompt", prompt == null ? "" : prompt);
        instances.add(instance);
        jsonBody.add("instances", instances);

        JsonObject parameters = new JsonObject();
        if (imagePrompt != null) {
            if (imagePrompt.getNumber() > 0) {
                parameters.addProperty("sampleCount", imagePrompt.getNumber());
            }
            if (imagePrompt.getSize() != null && !imagePrompt.getSize().isEmpty()) {
                JsonObject imageDimensions = parseImageDimensions(imagePrompt.getSize());
                if (imageDimensions != null) {
                    parameters.add("imageDimensions", imageDimensions);
                }
            }
        }

        if (parameters.size() > 0) {
            jsonBody.add("parameters", parameters);
        }

        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToTextToSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        String input = textToSpeechPrompt != null ? textToSpeechPrompt.getUserInput() : null;
        String voiceName = textToSpeechPrompt != null ? textToSpeechPrompt.getVoice() : null;
        String outputFormat = textToSpeechPrompt != null ? textToSpeechPrompt.getOutputFormat() : null;

        JsonObject inputObject = new JsonObject();
        inputObject.addProperty("text", input == null ? "" : input);
        jsonBody.add("input", inputObject);

        JsonObject voiceObject = new JsonObject();
        if (voiceName == null || voiceName.isEmpty() || "alloy".equalsIgnoreCase(voiceName)) {
            voiceName = "en-US-Neural2-A";
        }
        if (voiceName != null && !voiceName.isEmpty()) {
            voiceObject.addProperty("name", voiceName);
        }
        voiceObject.addProperty("languageCode", inferLanguageCode(voiceName));
        jsonBody.add("voice", voiceObject);

        JsonObject audioConfig = new JsonObject();
        audioConfig.addProperty("audioEncoding", mapAudioEncoding(outputFormat));
        jsonBody.add("audioConfig", audioConfig);

        return gson.toJson(jsonBody);
    }

    private JsonObject generateSystemInstruction(String model, AIModel.PromptStruct promptStruct) {
        if (!isSupportSystemHint(model)) {
            return null;
        }

        String systemHint = getDefaultSystemHint();
        if (promptStruct != null && promptStruct.getSystemHint() != null && !promptStruct.getSystemHint().isEmpty()) {
            systemHint = promptStruct.getSystemHint();
        }

        if (systemHint == null || systemHint.isEmpty()) {
            return null;
        }

        JsonObject instruction = new JsonObject();
        instruction.addProperty("role", "system");

        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", systemHint);
        parts.add(textPart);
        instruction.add("parts", parts);

        return instruction;
    }

    private JsonObject parseImageDimensions(String size) {
        if (size == null || size.isEmpty()) {
            return null;
        }

        String[] tokens = size.toLowerCase().split("x");
        if (tokens.length != 2) {
            return null;
        }

        try {
            int width = Integer.parseInt(tokens[0].trim());
            int height = Integer.parseInt(tokens[1].trim());

            if (width <= 0 || height <= 0) {
                return null;
            }

            JsonObject imageDimensions = new JsonObject();
            imageDimensions.addProperty("width", width);
            imageDimensions.addProperty("height", height);
            return imageDimensions;
        }
        catch (NumberFormatException ex) {
            logger.warn("Unable to parse image size " + size + ", ignore this setting.", ex);
            return null;
        }
    }

    private void collectImagePayloadFromPrediction(JsonObject predictionObject, List<String> collected) {
        if (predictionObject == null) {
            return;
        }

        String mimeType = extractMimeType(predictionObject, "image/png");

        extractBase64Strings(predictionObject, mimeType, collected,
                "bytesBase64Encoded", "base64Data", "base64", "imageBase64");

        if (predictionObject.has("b64Images") && predictionObject.get("b64Images").isJsonArray()) {
            JsonArray b64Images = predictionObject.getAsJsonArray("b64Images");
            for (int i = 0; i < b64Images.size(); i++) {
                if (b64Images.get(i).isJsonNull()) {
                    continue;
                }
                addBase64Image(b64Images.get(i).getAsString(), mimeType, collected);
            }
        }

        if (predictionObject.has("image") && !predictionObject.get("image").isJsonNull()) {
            JsonElement imageElement = predictionObject.get("image");
            if (imageElement.isJsonPrimitive()) {
                addBase64Image(imageElement.getAsString(), mimeType, collected);
            }
            else if (imageElement.isJsonObject()) {
                JsonObject imageObject = imageElement.getAsJsonObject();
                String nestedMimeType = extractMimeType(imageObject, mimeType);
                extractBase64Strings(imageObject, nestedMimeType, collected,
                        "bytesBase64Encoded", "base64Data", "base64", "imageBase64", "data");
                if (imageObject.has("inlineData") && imageObject.get("inlineData").isJsonObject()) {
                    JsonObject inlineData = imageObject.getAsJsonObject("inlineData");
                    collectInlineImageData(inlineData, nestedMimeType, collected);
                }
            }
        }

        if (predictionObject.has("images") && predictionObject.get("images").isJsonArray()) {
            JsonArray nestedImages = predictionObject.getAsJsonArray("images");
            for (int i = 0; i < nestedImages.size(); i++) {
                JsonElement item = nestedImages.get(i);
                if (item.isJsonNull()) {
                    continue;
                }
                if (item.isJsonPrimitive()) {
                    addBase64Image(item.getAsString(), mimeType, collected);
                }
                else if (item.isJsonObject()) {
                    collectImagePayloadFromPrediction(item.getAsJsonObject(), collected);
                }
            }
        }

        if (predictionObject.has("inlineData") && predictionObject.get("inlineData").isJsonObject()) {
            JsonObject inlineData = predictionObject.getAsJsonObject("inlineData");
            collectInlineImageData(inlineData, mimeType, collected);
        }
    }

    private void collectInlineImageData(JsonObject inlineData, String defaultMimeType, List<String> collected) {
        if (inlineData == null) {
            return;
        }

        String mimeType = extractMimeType(inlineData, defaultMimeType);
        if (inlineData.has("data") && !inlineData.get("data").isJsonNull()) {
            addBase64Image(inlineData.get("data").getAsString(), mimeType, collected);
        }
    }

    private void extractBase64Strings(JsonObject source, String mimeType, List<String> collected, String... keys) {
        if (source == null || keys == null) {
            return;
        }

        for (String key : keys) {
            if (key == null || !source.has(key) || source.get(key).isJsonNull()) {
                continue;
            }
            JsonElement value = source.get(key);
            if (value.isJsonPrimitive()) {
                addBase64Image(value.getAsString(), mimeType, collected);
            }
        }
    }

    private String extractMimeType(JsonObject source, String defaultMimeType) {
        if (source != null && source.has("mimeType") && !source.get("mimeType").isJsonNull()) {
            return source.get("mimeType").getAsString();
        }
        return defaultMimeType;
    }

    private void addBase64Image(String base64, String mimeType, List<String> collected) {
        if (base64 == null || base64.isEmpty()) {
            return;
        }
        String resolvedMime = (mimeType == null || mimeType.isEmpty()) ? "image/png" : mimeType;
        collected.add("data:" + resolvedMime + ";base64," + base64);
    }

    private String inferLanguageCode(String voiceName) {
        if (voiceName != null) {
            String[] segments = voiceName.split("-");
            if (segments.length >= 2) {
                return segments[0] + "-" + segments[1];
            }
        }
        return "en-US";
    }

    private String mapAudioEncoding(String outputFormat) {
        if (outputFormat == null || outputFormat.isEmpty()) {
            return "MP3";
        }

        String normalized = outputFormat.trim().toLowerCase();
        switch (normalized) {
            case "mp3":
                return "MP3";
            case "wav":
            case "linear16":
                return "LINEAR16";
            case "ogg":
            case "opus":
            case "ogg_opus":
                return "OGG_OPUS";
            case "flac":
                return "FLAC";
            default:
                return normalized.toUpperCase();
        }
    }

    private String generateJsonBodyToSpeechToText(String base64Audio, String filePath) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        JsonObject config = new JsonObject();
        config.addProperty("languageCode", "en-US");
        config.addProperty("enableAutomaticPunctuation", true);
        config.addProperty("encoding", inferSpeechEncoding(filePath));
        jsonBody.add("config", config);

        JsonObject audio = new JsonObject();
        audio.addProperty("content", base64Audio == null ? "" : base64Audio);
        jsonBody.add("audio", audio);

        return gson.toJson(jsonBody);
    }

    private String inferSpeechEncoding(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "MP3";
        }

        String name = new File(filePath).getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            String ext = name.substring(dotIndex + 1).toLowerCase();
            switch (ext) {
                case "wav":
                case "wave":
                    return "LINEAR16";
                case "flac":
                    return "FLAC";
                case "ogg":
                case "oga":
                case "opus":
                    return "OGG_OPUS";
                case "amr":
                    return "AMR";
                case "awb":
                    return "AMR_WB";
                case "mp3":
                default:
                    return "MP3";
            }
        }
        return "MP3";
    }

    private String sendPostWithFormData(String model, String filePath) throws Exception {
        logger.info("call google api, model = " + model + ", filePath = " + filePath);
        byte[] bytes = Files.readAllBytes(Path.of(filePath));
        String base64Audio = Base64.getEncoder().encodeToString(bytes);
        String jsonInput = generateJsonBodyToSpeechToText(base64Audio, filePath);
        return sendPost(model, jsonInput);
    }

    private String readResponseToString(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream in = (status >= 400)?conn.getErrorStream():conn.getInputStream();

        String body = CommonUtil.alignJson(IOUtil.inputStreamToString(in));
        logger.info("return from remote api");
        logger.info("HTTP " + status);
        logger.info("response = " + body);
        if(status == 429) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
        }
        else if(status >= 400) {
            throw new NeoAIException(body);
        }
        return body;
    }

    private String sendPost(String model, String jsonInput) throws Exception {
        return sendPostToUrl(getUrl(model), jsonInput, model);
    }

    private void sendPostAndGenerateFile(String model, String jsonInput, String filePath) throws Exception {
        String jsonResponse = sendPost(model, jsonInput);
        writeAudioContentToFile(jsonResponse, filePath);
    }

    private String sendPostToUrl(String requestUrl, String jsonInput, String model) throws Exception {
        jsonInput = CommonUtil.alignJson(jsonInput);
        URL url = new URL(requestUrl);
        logger.info("call remote api");
        logger.info("POST " + requestUrl);
        logger.info("body = " + jsonInput);
        logger.info("model = " + model);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-goog-api-key", getApiKey());
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            return readResponseToString(conn);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String buildCountTokensUrl(String model) {
        String baseUrl = getUrl(model);
        if (baseUrl.contains(":generateContent")) {
            return baseUrl.replace(":generateContent", ":countTokens");
        }
        return baseUrl;
    }

    private void writeAudioContentToFile(String jsonResponse, String filePath) throws Exception {
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            if (!element.isJsonObject()) {
                throw new NeoAIException("Text-to-speech response is not a JSON object");
            }
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.has("audioContent") && !jsonObject.get("audioContent").isJsonNull()) {
                String audioContent = jsonObject.get("audioContent").getAsString();
                byte[] audioBytes = Base64.getDecoder().decode(audioContent);
                IOUtil.bytesToFile(audioBytes, filePath);
                logger.info("return from google api, file [" + filePath + "] generated.");
                return;
            }

            if (jsonObject.has("error") && jsonObject.get("error").isJsonObject()) {
                JsonObject errorObject = jsonObject.getAsJsonObject("error");
                String errorMessage = errorObject.has("message") && !errorObject.get("message").isJsonNull()
                        ? errorObject.get("message").getAsString()
                        : "Unknown text-to-speech error";
                throw new NeoAIException(errorMessage);
            }

            throw new NeoAIException("Text-to-speech response missing audioContent");
        }
        catch (IllegalArgumentException ex) {
            throw new NeoAIException("Invalid base64 audio content", ex);
        }
        catch (JsonSyntaxException ex) {
            throw new NeoAIException("Invalid JSON when extracting text-to-speech response", ex);
        }
    }
}
