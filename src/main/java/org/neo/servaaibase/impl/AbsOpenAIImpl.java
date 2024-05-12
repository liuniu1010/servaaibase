package org.neo.servaaibase.impl;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.neo.servaframe.util.IOUtil;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;

abstract public class AbsOpenAIImpl implements SuperAIIFC {
    final static Logger logger = Logger.getLogger(AbsOpenAIImpl.class);

    abstract protected String getApiKey();
    abstract protected String getUrl(String model);
    abstract protected int getMaxOutputTokenNumber(String model);
    abstract protected int getContextWindow(String model);
    abstract protected String getSystemHint();

    @Override
    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct) {
        try {
            return innerFetchChatResponse(model, promptStruct, null);
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) {
        try {
            return innerFetchChatResponse(model, promptStruct, functionCallIFC);
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public AIModel.Embedding getEmbedding(String model, String input) {
        try {
            return innerGetEmbedding(model, input, -1);
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public AIModel.Embedding getEmbedding(String model, String input, int dimensions) {
        try {
            return innerGetEmbedding(model, input, dimensions);
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String[] generateImages(String model, AIModel.ImagePrompt imagePrompt) {
        try {
            return innerGenerateImage(model, imagePrompt);
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String generateSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileAbsolutePath) {
        try {
            return innerGenerateSpeech(model, textToSpeechPrompt, onlineFileAbsolutePath);
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public AIModel.ChatResponse audioToText(String model, AIModel.Attachment attachment) {
        try {
            return innerAudioToText(model, attachment);
        }
        catch(RuntimeException rex) {
            logger.error(rex.getMessage(), rex);
            throw rex;
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) throws Exception {
        int maxTokens = determineMaxTokens(model, promptStruct, functionCallIFC);
        AIModel.ChatResponse chatResponse = innerFetchChatResponse(model, promptStruct, maxTokens, functionCallIFC);
        return chatResponse;
    }

    private String[] innerGenerateImage(String model, AIModel.ImagePrompt imagePrompt) throws Exception {
        String jsonInput = generateJsonBodyToGenerateImage(model, imagePrompt);
        String jsonResponse = send(model, jsonInput);
        String[] urls = extractImageUrlsFromJson(jsonResponse);
        return urls;
    }

    private String innerGenerateSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileAbsolutePath) throws Exception {
        String jsonInput = generateJsonBodyToGenerateSpeech(model, textToSpeechPrompt);
        String fileName = "audio_" + CommonUtil.getRandomString(10) + "." + textToSpeechPrompt.getOutputFormat();
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;
        sendAndGenerateFile(model, jsonInput, filePath);
        return fileName;
    }

    private AIModel.ChatResponse innerAudioToText(String model, AIModel.Attachment attachment) throws Exception {
        String jsonInput = generateJsonBodyForAudioToText(model, attachment);
        String jsonResponse = send(model, jsonInput);
        AIModel.ChatResponse chatResponse = extractTextFromAudioJson(jsonResponse);
        return chatResponse;
    }

    private AIModel.Embedding innerGetEmbedding(String model, String input, int dimensions) throws Exception {
        String jsonInput = generateJsonBodyToGetEmbedding(model, input, dimensions);
        String jsonResponse = send(model, jsonInput);
        AIModel.Embedding embedding = extractEmbeddingFromJson(jsonResponse);
        return embedding;
    }

    private int determineMaxTokens(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) throws Exception {
        boolean needCalculate = false; // calculate prompt token number or not
        if(needCalculate) { // in this way, calculate prompt token number first
            int promptTokenNumber = fetchPromptTokenNumber(model, promptStruct, functionCallIFC);
            if(promptTokenNumber < 0) {
                throw new RuntimeException("some error occurred for promptTokenNumber < 0");
            }

            return Math.min(getMaxOutputTokenNumber(model), (getContextWindow(model) - promptTokenNumber));
        }
        else {
            return getMaxOutputTokenNumber(model); // in this way, don't calcuate prompt token number
        }
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens, FunctionCallIFC functionCallIFC) throws Exception {
        String jsonInput = generateJsonBodyToFetchResponse(model, promptStruct, maxTokens, functionCallIFC);
        String jsonResponse = send(model, jsonInput);
        List<AIModel.Call> calls = extractCallsFromJson(jsonResponse);
        AIModel.ChatResponse chatResponse = extractChatResponseFromJson(jsonResponse);
        chatResponse.setCalls(calls);
        return chatResponse;
    }

    private int fetchPromptTokenNumber(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) throws Exception {
        String jsonInput = generateJsonBodyForGetTokenNumber(model, promptStruct, functionCallIFC);
        String jsonTokenNumber = send(model, jsonInput);
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
                    call.setMethodName(functionObject.get("name").getAsString());

                    String argumentsString = functionObject.get("arguments").getAsString();
                    JsonElement elementArguments = JsonParser.parseString(argumentsString);
                    JsonObject argumentsObject = elementArguments.getAsJsonObject();
                    if(argumentsObject != null
                        && !argumentsObject.isJsonNull()) {
                        Set<String> paramNames = argumentsObject.keySet();
                        List<AIModel.CallParam> callParams = new ArrayList<AIModel.CallParam>();
                        for(String paramName: paramNames) {
                            AIModel.CallParam callParam = new AIModel.CallParam();
                            callParam.setName(paramName);
                            callParam.setValue(argumentsObject.get(paramName).getAsString());
                            callParams.add(callParam);
                        }
                        call.setParams(callParams);
                    }
                    calls.add(call);
                }
            }
        }
        return calls; 
    }

    private AIModel.ChatResponse extractTextFromAudioJson(String jsonResponse) throws Exception {
        // to be implemented
        return null;
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

    private JsonArray generateJsonArrayTools(FunctionCallIFC functionCallIFC) {
        JsonArray tools = new JsonArray();

        List<AIModel.Function> functions = functionCallIFC.getFunctions();
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

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", getSystemHint());
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

    private String generateJsonBodyForGetTokenNumber(String model, AIModel.PromptStruct promptStruct, FunctionCallIFC functionCallIFC) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("max_tokens", 1);
        jsonBody.addProperty("temperature", 0);
        jsonBody.addProperty("n", 1);
        jsonBody.addProperty("stop", "");

        JsonArray messages = generateJsonArrayMessages(model, promptStruct);
        jsonBody.add("messages", messages);

        if(functionCallIFC != null) {
            JsonArray tools = generateJsonArrayTools(functionCallIFC);
            jsonBody.add("tools", tools);
        }
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToFetchResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens, FunctionCallIFC functionCallIFC) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("max_tokens", maxTokens);
        jsonBody.addProperty("temperature", 0.5);

        JsonArray messages = generateJsonArrayMessages(model, promptStruct);
        jsonBody.add("messages", messages);

        if(functionCallIFC != null) {
            JsonArray tools = generateJsonArrayTools(functionCallIFC);
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

    private String generateJsonBodyToGenerateSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();

        jsonBody.addProperty("model", model);
        jsonBody.addProperty("input", textToSpeechPrompt.getUserInput());
        jsonBody.addProperty("voice", textToSpeechPrompt.getVoice());
        jsonBody.addProperty("output_format", textToSpeechPrompt.getOutputFormat());

        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyForAudioToText(String model, AIModel.Attachment attachment) {
        // to be implement
        return null;
    }

    private void sendAndGenerateFile(String model, String jsonInput, String filePath) throws Exception {
        logger.debug("call openai api, model = " + model + ", jsonInput = " + jsonInput);
        URL url = new URL(getUrl(model));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + getApiKey());

            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()){
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            try (InputStream in = connection.getInputStream()){
                IOUtil.inputStreamToFile(in, filePath);
                logger.debug("return from openai api, file [" + filePath +"] generated."); 
            }
        }
        catch(Exception ex) {
            logger.error("get exeception in sending, " + ex.getMessage(), ex);
            try (InputStream errIn = connection.getErrorStream()) {
                if(errIn != null) {
                    String errorResponse = IOUtil.inputStreamToString(errIn);
                    logger.error("get exception from openai api, response = " + errorResponse);
                }
            }
            throw ex;
        }
        finally {
            connection.disconnect();
        }
    }

    private String send(String model, String jsonInput) throws Exception {
        logger.debug("call openai api, model = " + model + ", jsonInput = " + jsonInput);
        URL url = new URL(getUrl(model));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + getApiKey());

            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()){
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            try (InputStream in = connection.getInputStream()){
                String response = IOUtil.inputStreamToString(in);
                logger.debug("return from openai api, response = " + response);
                return response;
            }
        }
        catch(Exception ex) {
            try (InputStream errIn = connection.getErrorStream()) {
                String errorResponse = IOUtil.inputStreamToString(errIn);
                logger.error("get exception from openai api, response = " + errorResponse);
            }
            throw ex;
        }
        finally {
            connection.disconnect();
        }
    }
}
