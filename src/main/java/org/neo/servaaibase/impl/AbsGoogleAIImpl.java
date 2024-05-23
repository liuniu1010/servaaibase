package org.neo.servaaibase.impl;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

abstract public class AbsGoogleAIImpl implements SuperAIIFC {
    final static Logger logger = Logger.getLogger(AbsGoogleAIImpl.class);

    abstract protected String getApiKey();
    abstract protected String getUrl(String model, String action);
    abstract protected int getMaxOutputTokenNumber(String model);
    abstract protected int getMaxInputTokenNumber(String model);

    @Override
    public AIModel.ChatResponse fetchChatResponse(String model, AIModel.PromptStruct promptStruct) {
        try {
            return innerFetchChatResponse(model, promptStruct);
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

    /*
     * it is not support to reduce dimensions currently
     */  
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
        // to be implemented
        return null;
    }

    @Override
    public AIModel.ChatResponse speechToText(String model, AIModel.Attachment attachment) {
        // to be implemented
        return null;
    }

    private String[] innerGenerateImage(String model, AIModel.ImagePrompt imagePrompt) throws Exception {
        String jsonInput = generateJsonBodyToGenerateImage(model, imagePrompt);
        String url = getUrl(model, "generateImage");   // adjust later
        String jsonResponse = send(model, url, jsonInput);
        String[] urls = extractImageUrlsFromJson(jsonResponse);
        return urls;
    }

    private AIModel.Embedding innerGetEmbedding(String model, String input, int dimensions) throws Exception {
        String jsonInput = generateJsonBodyToGetEmbedding(model, input, dimensions);
        String url = getUrl(model, "embedContent");
        String jsonResponse = send(model, url, jsonInput);
        AIModel.Embedding embedding = extractEmbeddingFromJson(jsonResponse);
        return embedding;
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct) throws Exception {
        int maxTokens = determineMaxTokens(model, promptStruct);
        AIModel.ChatResponse chatResponse = innerFetchChatResponse(model, promptStruct, maxTokens);
        return chatResponse;
    }

    private AIModel.ChatResponse innerFetchChatResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens) throws Exception {
        String jsonInput = generateJsonBodyToFetchResponse(model, promptStruct, maxTokens);
        String url = getUrl(model, "generateContent");
        String jsonResponse = send(model, url, jsonInput);
        List<AIModel.Call> calls = extractCallsFromJson(jsonResponse);
        AIModel.ChatResponse chatResponse = extractChatResponseFromJson(jsonResponse);
        chatResponse.setCalls(calls);
        return chatResponse;
    }

    private int determineMaxTokens(String model, AIModel.PromptStruct promptStruct) throws Exception {
        return getMaxOutputTokenNumber(model);
    }

    private AIModel.Embedding extractEmbeddingFromJson(String jsonResponse) {
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();

        JsonArray dataArray = jsonObject.getAsJsonObject("embedding").getAsJsonArray("values");

        int size = dataArray.size();
        double[] data = new double[size];
        for(int i = 0;i < size;i++) {
            data[i] = dataArray.get(i).getAsDouble();
        }

        AIModel.Embedding embedding = new AIModel.Embedding(data);
        return embedding;
    }

    private String[] extractImageUrlsFromJson(String jsonResponse) {
        // to be implemented
        return null;
    }

    private List<AIModel.Call> extractCallsFromJson(String jsonResponse) throws Exception {
        List<AIModel.Call> calls = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("candidates")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("candidates");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject contentObject = firstChoice.getAsJsonObject("content");
            JsonArray partsArray = contentObject.getAsJsonArray("parts");
            JsonObject firstPart = partsArray.get(0).getAsJsonObject();

            if(firstPart.get("functionCall") == null
                || firstPart.get("functionCall").isJsonNull()) {
                return null;
            }

            calls = new ArrayList<AIModel.Call>();
            JsonObject functionCall = firstPart.getAsJsonObject("functionCall");
            AIModel.Call call = new AIModel.Call();
            call.setMethodName(functionCall.get("name").getAsString());

            JsonObject argumentsObject = functionCall.getAsJsonObject("args");
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

        return calls;
    }

    private int fetchPromptTokenNumber(String model, AIModel.PromptStruct promptStruct) throws Exception {
        String jsonInput = generateJsonBodyForGetTokenNumber(model, promptStruct);
        String url = getUrl(model, "countTokens");
        String jsonTokenNumber = send(model, url, jsonInput);
        int tokenNumber = extractTokenNumberFromJson(jsonTokenNumber);
        return tokenNumber;
    }

    private int extractTokenNumberFromJson(String jsonTokenNumber) {
        JsonElement element = JsonParser.parseString(jsonTokenNumber);
        JsonObject jsonObject = element.getAsJsonObject();
        int tokenNumber = jsonObject.get("totalTokens").getAsInt();
        return tokenNumber;
    }

    private String generateJsonBodyForGetTokenNumber(String model, AIModel.PromptStruct promptStruct) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        JsonArray jsonContents = generateJsonArrayContents(model, promptStruct);
        jsonBody.add("contents", jsonContents);

        if(promptStruct.getFunctionCall() != null) {
            JsonArray tools = generateJsonArrayTools(promptStruct.getFunctionCall());
            jsonBody.add("tools", tools);
        }

        return gson.toJson(jsonBody);
    }

    private AIModel.ChatResponse extractChatResponseFromJson(String jsonResponse) throws Exception {
        AIModel.ChatResponse chatResponse = null;
        JsonElement element = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("candidates")) {
            JsonArray choicesArray = jsonObject.getAsJsonArray("candidates");
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            JsonObject contentObject = firstChoice.getAsJsonObject("content");
            JsonArray partsArray = contentObject.getAsJsonArray("parts");
            JsonObject firstPart = partsArray.get(0).getAsJsonObject();
            String text = "";
            if(firstPart.get("text") != null
                && !firstPart.get("text").isJsonNull()) {
                text = firstPart.get("text").getAsString();
            }

            chatResponse = new AIModel.ChatResponse(true, text);
        }
        else {
            String errorMessage = jsonObject.getAsJsonObject("error").get("message").getAsString();
            chatResponse = new AIModel.ChatResponse(false, errorMessage);
        }
        return chatResponse;
    }

    private JsonObject generateJsonObjectFromChatRecord(AIModel.ChatRecord chatRecord) {
        JsonArray jsonParts = new JsonArray();
        JsonObject jsonText = new JsonObject();
        jsonText.addProperty("text", chatRecord.getContent());
        jsonParts.add(jsonText);

        JsonObject recordContent = new JsonObject();
        recordContent.addProperty("role", chatRecord.getIsRequest()?"user":"model");
        recordContent.add("parts", jsonParts);

        return recordContent;
    }

    private JsonObject generateJsonObjectGenerationConfig(int maxTokens) {
        JsonObject jsonGenerationConfig = new JsonObject();
        jsonGenerationConfig.addProperty("maxOutputTokens", maxTokens); 
        jsonGenerationConfig.addProperty("temperature", 0.5); 

        return jsonGenerationConfig;
    }

    private JsonObject generateJsonContentForEmbedding(String model, String input) {
        JsonObject jsonContent = new JsonObject();

        JsonArray jsonParts = new JsonArray();
        JsonObject jsonText = new JsonObject();
        jsonText.addProperty("text", input);
        jsonParts.add(jsonText);

        jsonContent.add("parts", jsonParts);

        return jsonContent;
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

    private JsonArray generateJsonArrayContents(String model, AIModel.PromptStruct promptStruct) {
        JsonArray jsonContents = new JsonArray();

        JsonArray jsonUserParts = new JsonArray();

        if(!isVisionModel(model)) {
            // conversation history parts
            List<AIModel.ChatRecord> chatRecords = promptStruct.getChatRecords();
            boolean beginWithUserRole = false;
            for(AIModel.ChatRecord chatRecord: chatRecords) {
                // for google api request, the first and last end text should be role user
                // so the first end text with role model should be filtered out
                if(chatRecord.getIsRequest()) {
                    beginWithUserRole = true;
                }
            
                if(beginWithUserRole) {
                    jsonContents.add(generateJsonObjectFromChatRecord(chatRecord));
                }
            }
        }
        else { // current google vision model doesn't support multi turn
            // inlineData of part
            AIModel.AttachmentGroup attachmentGroup = promptStruct.getAttachmentGroup();
            if(attachmentGroup != null
                && attachmentGroup.getAttachments() != null) {
                List<AIModel.Attachment> attachments = attachmentGroup.getAttachments();
                for(AIModel.Attachment attachment: attachments) {
                    String mimeType = CommonUtil.extractMimeTypeFromBase64(attachment.getContent());
                    if(mimeType == null) {
                        throw new RuntimeException("the attachment doesn't have mime type");
                    }

                    JsonObject jsonUserPartOnInline = new JsonObject();
                    JsonObject jsonInlineData = new JsonObject();
                    jsonInlineData.addProperty("mime_type", mimeType);
                    jsonInlineData.addProperty("data", CommonUtil.extractRawBase64(attachment.getContent()));
                    jsonUserPartOnInline.add("inlineData", jsonInlineData);

                    jsonUserParts.add(jsonUserPartOnInline);
                }
            }
        }

        // text of part
        JsonObject jsonUserPartOnText = new JsonObject();
        String adjustedInput = promptStruct.getUserInput();
        if(promptStruct.getSystemHint() != null
            && !promptStruct.getSystemHint().isEmpty()) {
            adjustedInput = promptStruct.getSystemHint() + "\n" + promptStruct.getUserInput(); 
        }
        jsonUserPartOnText.addProperty("text", adjustedInput);
        jsonUserParts.add(jsonUserPartOnText);

        JsonObject userInputContent = new JsonObject();
        userInputContent.addProperty("role", "user");
        userInputContent.add("parts", jsonUserParts);

        jsonContents.add(userInputContent);

        return jsonContents;
    }


    private String generateJsonBodyToFetchResponse(String model, AIModel.PromptStruct promptStruct, int maxTokens) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        JsonArray jsonContents = generateJsonArrayContents(model, promptStruct);
        jsonBody.add("contents", jsonContents);

        JsonObject jsonGenerationConfig = generateJsonObjectGenerationConfig(maxTokens);
        jsonBody.add("generationConfig", jsonGenerationConfig);

        if(promptStruct.getFunctionCall() != null) {
            JsonArray tools = generateJsonArrayTools(promptStruct.getFunctionCall());
            jsonBody.add("tools", tools);
        }

        return gson.toJson(jsonBody);
    }

    private JsonArray generateJsonArrayTools(FunctionCallIFC functionCall) {
        JsonArray tools = new JsonArray();

        List<AIModel.Function> functions = functionCall.getFunctions();
        for(AIModel.Function function: functions) {
            JsonObject jsonFunctionDeclaration = new JsonObject();
            jsonFunctionDeclaration.addProperty("name", function.getMethodName());
            jsonFunctionDeclaration.addProperty("description", function.getDescription());

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
            jsonFunctionDeclaration.add("parameters", jsonParameters);

            JsonObject jsonTool = new JsonObject();
            jsonTool.add("functionDeclarations", jsonFunctionDeclaration);

            tools.add(jsonTool);
        }

        return tools;
    }

    private String generateJsonBodyToGetEmbedding(String model, String input, int dimensions) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "models/" + model);
        JsonObject jsonContent = generateJsonContentForEmbedding(model, input);
        jsonBody.add("content", jsonContent);

        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyToGenerateImage(String model, AIModel.ImagePrompt imagePrompt) {
        // to be implemented
        return null;
    }

    private String send(String model, String url, String jsonInput) throws Exception {
        logger.debug("call googleai api, model = " + model + ", jsonInput = " + jsonInput);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()){
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            try (InputStream in = connection.getInputStream()){
                String response = IOUtil.inputStreamToString(in);
                logger.debug("return from googleai api, response = " + response);
                return response;
            }
        }
        catch(Exception ex) {
            try (InputStream errIn = connection.getErrorStream()) {
                String errorResponse = IOUtil.inputStreamToString(errIn);
                logger.error("get exception from googleai api, response = " + errorResponse);
            }
            throw ex;
        }
        finally {
            connection.disconnect();
        }
    }
}
