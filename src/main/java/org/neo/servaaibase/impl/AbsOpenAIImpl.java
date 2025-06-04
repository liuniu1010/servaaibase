package org.neo.servaaibase.impl;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;

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

abstract public class AbsOpenAIImpl implements SuperAIIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbsOpenAIImpl.class);

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
        String jsonTokenNumber = sendPost(model, jsonInput);
        int tokenNumber = extractTokenNumberFromJson(jsonTokenNumber);
        return tokenNumber;
    }

    private int extractTokenNumberFromJson(String jsonTokenNumber) {
        JsonElement element = JsonParser.parseString(jsonTokenNumber);
        JsonObject jsonObject = element.getAsJsonObject();
        int tokenNumber = jsonObject.getAsJsonObject("usage").get("prompt_tokens").getAsInt();
        return tokenNumber;
    }

    private AIModel.Embedding extractEmbeddingFromJson(String jsonResponse) {
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();

        JsonArray dataArray = jsonObject.getAsJsonArray("data").get(0).getAsJsonObject().getAsJsonArray("embedding");

        int size = dataArray.size();
        double[] data = new double[size];
        for(int i = 0;i < size;i++) {
            data[i] = dataArray.get(i).getAsDouble();
        }

        AIModel.Embedding embedding = new AIModel.Embedding(data);
        return embedding;
    }

    private String[] extractImageUrlsFromJson(String jsonResponse) {
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();

        JsonArray jsonData = jsonObject.getAsJsonArray("data");
        String[] urls = new String[jsonData.size()];
        for(int i = 0;i < jsonData.size();i++) {
            String url = jsonData.get(i).getAsJsonObject().get("url").getAsString();
            urls[i] = url;
        }

        return urls;
    }

    private AIModel.TokensUsage extractTokensUsageFromJson(String jsonResponse) throws Exception {
        AIModel.TokensUsage tokensUsage = new AIModel.TokensUsage();
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("usage")) {
            JsonObject usage = jsonObject.getAsJsonObject("usage");
            tokensUsage.setInputTokens(usage.get("prompt_tokens").getAsInt());
            if(usage.has("completion_tokens")) {
                tokensUsage.setOutputTokens(usage.get("completion_tokens").getAsInt());
                if(usage.has("prompt_tokens_details")) {
                    JsonObject promptTokensDetails = usage.getAsJsonObject("prompt_tokens_details");
                    tokensUsage.setCachedTokens(promptTokensDetails.get("cached_tokens").getAsInt());
                }
            }
        }

        return tokensUsage;
    }

    private List<AIModel.Call> extractCallsFromJson(String jsonResponse) throws Exception {
        List<AIModel.Call> calls = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("choices")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("choices");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject messageObject = firstChoice.getAsJsonObject("message");

            if(!messageObject.has("tool_calls")) {
                return null;
            }
            JsonArray toolCallsArray = messageObject.getAsJsonArray("tool_calls");
            if(toolCallsArray != null
                && !toolCallsArray.isJsonNull()
                && toolCallsArray.size() > 0) {
                calls = new ArrayList<AIModel.Call>();
                for(int i = 0;i < toolCallsArray.size();i++) {
                    JsonObject tool = toolCallsArray.get(i).getAsJsonObject();
                    if(!tool.get("type").getAsString().equals("function")) {
                        continue;
                    }

                    JsonObject functionObject = tool.getAsJsonObject("function");
                    AIModel.Call call = new AIModel.Call();
                    String methodName = functionObject.get("name").getAsString();
                    if(methodName.startsWith("multi_tool_use")) {
                        // not support this case
                        continue;
                    }
                    call.setMethodName(methodName);

                    String argumentsString = functionObject.get("arguments").getAsString();
                    List<AIModel.CallParam> callParams = extractArgumentsString(argumentsString);
                    call.setParams(callParams);
                    calls.add(call);
                }
            }
        }
        return calls; 
    }

    private boolean isJsonString(String argumentsString) {
        try {
            JsonParser.parseString(argumentsString);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    // it seems this is openai's bug, handle possible bugs in this method
    private List<AIModel.CallParam> extractArgumentsString(String argumentsString) {
        List<AIModel.CallParam> callParams = new ArrayList<AIModel.CallParam>();
        if(isJsonString(argumentsString)) { // by design it should be a json string, sometimes it return a normal string
            JsonElement elementArguments = JsonParser.parseString(argumentsString);
            JsonObject argumentsObject = elementArguments.getAsJsonObject();
            if(argumentsObject != null
                && !argumentsObject.isJsonNull()) {
                Set<String> paramNames = argumentsObject.keySet();
                for(String paramName: paramNames) {
                    AIModel.CallParam callParam = new AIModel.CallParam();
                    callParam.setName(paramName);
                    callParam.setValue(argumentsObject.get(paramName).getAsString());
                    callParams.add(callParam);
                }
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
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        String text = jsonObject.get("text").getAsString();
        return new AIModel.ChatResponse(true, text);
    }

    private AIModel.ChatResponse extractChatResponseFromJson(String jsonResponse) throws Exception {
        AIModel.ChatResponse chatResponse = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("choices")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("choices");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject messageObject = firstChoice.getAsJsonObject("message");
            String content = "";
            if(messageObject.get("content") != null 
                && !messageObject.get("content").isJsonNull()) {
                content = messageObject.get("content").getAsString();
            }

            chatResponse = new AIModel.ChatResponse(true, content);
        }
        else {
            String errorMessage = jsonObject.getAsJsonObject("error").get("message").getAsString();

            chatResponse = new AIModel.ChatResponse(false, errorMessage);
        }
        return chatResponse; 
    }

    private JsonArray generateJsonArrayTools(FunctionCallIFC functionCall) {
        JsonArray tools = new JsonArray();

        List<AIModel.Function> functions = functionCall.getFunctions();
        for(AIModel.Function function: functions) {
            JsonObject jsonFunction = new JsonObject();
            jsonFunction.addProperty("name", function.getMethodName());
            jsonFunction.addProperty("description", function.getDescription());

            JsonObject jsonParameters = new JsonObject();
            jsonParameters.addProperty("type", "object");
            List<AIModel.FunctionParam> functionParams = function.getParams();
            JsonObject jsonProperties = new JsonObject();
            JsonArray jsonRequiredParams = new JsonArray();
            for(AIModel.FunctionParam functionParam: functionParams) {
                JsonObject jsonParam = new JsonObject();
                jsonParam.addProperty("type", "string");
                jsonParam.addProperty("description", functionParam.getDescription());

                jsonProperties.add(functionParam.getName(), jsonParam);
                jsonRequiredParams.add(functionParam.getName()); 
            }
            jsonParameters.add("properties", jsonProperties);
            jsonParameters.add("required", jsonRequiredParams);
            jsonFunction.add("parameters", jsonParameters);

            JsonObject jsonTool = new JsonObject();
            jsonTool.addProperty("type", "function");
            jsonTool.add("function", jsonFunction);

            tools.add(jsonTool);
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
        JsonArray messages = new JsonArray();
        String systemHint = getDefaultSystemHint();
        if(promptStruct.getSystemHint() != null
            && !promptStruct.getSystemHint().isEmpty()) {
            systemHint = promptStruct.getSystemHint();  // caller has set system hint, use it
        }

        JsonObject systemMessage = new JsonObject();
        if(isSupportSystemHint(model)) {
            systemMessage.addProperty("role", "system");
        }
        else {
            systemMessage.addProperty("role", "user");
        }
        systemMessage.addProperty("content", systemHint);
        messages.add(systemMessage);

        List<AIModel.ChatRecord> chatRecords = promptStruct.getChatRecords();
        for(AIModel.ChatRecord chatRecord: chatRecords) {
            JsonObject recordMessage = new JsonObject();
            recordMessage.addProperty("role", chatRecord.getIsRequest()?"user":"assistant");
            recordMessage.addProperty("content", chatRecord.getContent());
            messages.add(recordMessage);
        }

        JsonObject userInputMessage = new JsonObject();
        userInputMessage.addProperty("role", "user");
        if(isVisionModel(model)) {
            JsonArray jsonContentArray = new JsonArray();

            JsonObject jsonTextContent = new JsonObject();
            jsonTextContent.addProperty("type", "text");
            jsonTextContent.addProperty("text", promptStruct.getUserInput());

            jsonContentArray.add(jsonTextContent);

            AIModel.AttachmentGroup attachmentGroup = promptStruct.getAttachmentGroup();
            if(attachmentGroup != null
                && attachmentGroup.getAttachments() != null) {
                List<AIModel.Attachment> attachments = attachmentGroup.getAttachments();
                for(AIModel.Attachment attachment: attachments) {
                    JsonObject jsonUrl = new JsonObject();
                    jsonUrl.addProperty("url", attachment.getContent());

                    JsonObject jsonImage = new JsonObject();
                    jsonImage.addProperty("type", "image_url");
                    jsonImage.add("image_url", jsonUrl);

                    jsonContentArray.add(jsonImage);
                }
            }

            userInputMessage.add("content", jsonContentArray);
        }
        else { // is chat model
            userInputMessage.addProperty("content", promptStruct.getUserInput());
        }
        messages.add(userInputMessage);

        return messages;
    }

    private String generateJsonBodyForGetTokenNumber(String model, AIModel.PromptStruct promptStruct) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("max_tokens", 1);
        jsonBody.addProperty("temperature", 0);
        jsonBody.addProperty("n", 1);
        jsonBody.addProperty("stop", "");

        JsonArray messages = generateJsonArrayMessages(model, promptStruct);
        jsonBody.add("messages", messages);

        if(promptStruct.getFunctionCall() != null) {
            JsonArray tools = generateJsonArrayTools(promptStruct.getFunctionCall());
            jsonBody.add("tools", tools);
        }
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToFetchResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        JsonArray messages = generateJsonArrayMessages(model, promptStruct);
        jsonBody.add("messages", messages);

        if(promptStruct.getFunctionCall() != null) {
            JsonArray tools = generateJsonArrayTools(promptStruct.getFunctionCall());
            jsonBody.add("tools", tools);
        }
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToGetEmbedding(String model, String input, int dimensions) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("input", input);
        if(dimensions > 0) {
            jsonBody.addProperty("dimensions", dimensions);
        }

        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToGenerateImage(String model, AIModel.ImagePrompt imagePrompt) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("prompt", imagePrompt.getUserInput());
        jsonBody.addProperty("size", imagePrompt.getSize());
        jsonBody.addProperty("n", imagePrompt.getNumber());

        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToTextToSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("input", textToSpeechPrompt.getUserInput());
        jsonBody.addProperty("voice", textToSpeechPrompt.getVoice());
        jsonBody.addProperty("output_format", textToSpeechPrompt.getOutputFormat());

        return gson.toJson(jsonBody);
    }

    private String sendPostWithFormData(String model, String filePath) throws Exception {
        logger.info("call openai api, model = " + model + ", filePath = " + filePath);
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
        
        String fileName = CommonUtil.getFileName(filePath);
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, RequestBody.create(new File(filePath), MediaType.parse("audio/mpeg")))
            .addFormDataPart("model", model)
            .build();

        Request request = new Request.Builder()
                .url(getUrl(model))
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            int status = response.code();
            if(status == 429) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
            }

            if (!response.isSuccessful()) {
                throw new NeoAIException("Unexpected code " + response);
            }

            String responseJson = response.body().string();
            responseJson = CommonUtil.alignJson(responseJson);
            logger.info("return from openai api, response = " + responseJson);
            return responseJson;
        }
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
            String message = IOUtil.inputStreamToString(in);
            throw new NeoAIException(message);
        }
        return body;
    }

    private void readResponseToFile(HttpURLConnection conn, String filePath) throws IOException {
        int status = conn.getResponseCode();
        InputStream in = (status >= 400)?conn.getErrorStream():conn.getInputStream();

        if(status == 429) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LLM_TOO_BUSY);
        }
        else if(status >= 400) {
            String message = IOUtil.inputStreamToString(in);
            throw new NeoAIException(message);
        }

        IOUtil.inputStreamToFile(in, filePath);
        logger.info("return from openai api, file [" + filePath +"] generated."); 
    }

    private String sendPost(String model, String jsonInput) throws Exception {
        jsonInput = CommonUtil.alignJson(jsonInput);
        String sUrl = getUrl(model);
        URL url = new URL(sUrl);
        logger.info("call remote api"); 
        logger.info("POST " + sUrl);
        logger.info("body = " + jsonInput);
        logger.info("model = " + model);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + getApiKey());
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

    private void sendPostAndGenerateFile(String model, String jsonInput, String filePath) throws Exception {
        jsonInput = CommonUtil.alignJson(jsonInput);
        String sUrl = getUrl(model);
        URL url = new URL(sUrl);
        logger.info("call remote api"); 
        logger.info("POST " + sUrl);
        logger.info("body = " + jsonInput);
        logger.info("model =" + model);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + getApiKey());
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            readResponseToFile(conn, filePath);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
