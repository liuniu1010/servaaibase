package org.neo.servaaibase.impl;

import java.util.Map;
import java.util.HashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;

public class OpenAIImpl extends AbsOpenAIImpl {
    protected DBConnectionIFC dbConnection;

    protected OpenAIImpl() {
    }

    protected OpenAIImpl(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
        setup();
    }

    public static OpenAIImpl getInstance(DBConnectionIFC inputDBConnection) {
        return new OpenAIImpl(inputDBConnection);
    }

    private static String gpt_4_turbo_preview = "gpt-4-turbo-preview";
    private static String gpt_35_turbo = "gpt-3.5-turbo";
    private static String text_embedding_3_large = "text-embedding-3-large";
    private static String text_embedding_3_small = "text-embedding-3-small";
    private static String dall_e_3 = "dall-e-3";
    private static String dall_e_2 = "dall-e-2";
    private static String gpt_4_vision_preview = "gpt-4-vision-preview";
    private static String tts_1 = "tts-1";
    private static String tts_1_hd = "tts-1-hd";

    private String[] chatModels;
    private String[] embeddingModels;
    private String[] imageModels;
    private String[] visionModels;
    private String[] textToSpeechModels;

    private Map<String, String> urlMapping;
    private Map<String, Integer> contextWindowMapping;
    private Map<String, Integer> maxOutputMapping;

    protected void setup() {
        chatModels = new String[]{gpt_4_turbo_preview, gpt_35_turbo};
        embeddingModels = new String[]{text_embedding_3_large, text_embedding_3_small};
        imageModels = new String[]{dall_e_3, dall_e_2};
        visionModels = new String[]{gpt_4_vision_preview};
        textToSpeechModels = new String[]{tts_1, tts_1_hd}; 

        urlMapping = new HashMap<String, String>();
        urlMapping.put(gpt_4_turbo_preview, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_35_turbo, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(text_embedding_3_large, "https://api.openai.com/v1/embeddings");
        urlMapping.put(text_embedding_3_small, "https://api.openai.com/v1/embeddings");
        urlMapping.put(dall_e_3, "https://api.openai.com/v1/images/generations");
        urlMapping.put(dall_e_2, "https://api.openai.com/v1/images/generations");
        urlMapping.put(gpt_4_vision_preview, "https://api.openai.com/v1/chat/completions");

        contextWindowMapping = new HashMap<String, Integer>();
        contextWindowMapping.put(gpt_4_turbo_preview, 128000);
        contextWindowMapping.put(gpt_35_turbo, 16385);
        contextWindowMapping.put(gpt_4_vision_preview, 128000);

        maxOutputMapping = new HashMap<String, Integer>();
        maxOutputMapping.put(gpt_4_turbo_preview, 4096);
        maxOutputMapping.put(gpt_35_turbo, 4096);
        maxOutputMapping.put(gpt_4_vision_preview, 4096);
    }

    @Override
    protected String getApiKey() {
        try {
            return CommonUtil.getConfigValue(dbConnection, "OpenAiApiKey");
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String[] getChatModels() {
        return chatModels;
    }

    @Override
    public String[] getEmbeddingModels() {
        return embeddingModels;
    }

    @Override
    public String[] getImageModels() {
        return imageModels;
    }

    @Override
    public String[] getVisionModels() {
        return visionModels;
    }

    @Override
    public String[] getTextToSpeechModels() {
        return textToSpeechModels;
    }

    @Override
    protected String getUrl(String model) {
        if(urlMapping.containsKey(model)) {
            return urlMapping.get(model);
        }
        else {
            throw new RuntimeException("model " + model + " not supported to get url!");
        }
    }

    @Override
    protected int getContextWindow(String model) {
        if(contextWindowMapping.containsKey(model)) {
            return contextWindowMapping.get(model);
        }
        else {
            throw new RuntimeException("model " + model + " not supported to get context window!");
        }
    }

    @Override
    protected int getMaxOutputTokenNumber(String model) {
        if(maxOutputMapping.containsKey(model)) {
            return maxOutputMapping.get(model);
        }
        else {
            throw new RuntimeException("model " + model + " not supported to get max output tokens!");
        }
    }

    @Override
    protected String getSystemHint() {
        return "You are a helpful assistant. You always response result in plain text.";
    }
}
