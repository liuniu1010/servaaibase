package org.neo.servaaibase.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

public class OpenAIImpl extends AbsOpenAIImpl {
    protected DBConnectionIFC dbConnection;

    protected OpenAIImpl() {
        setup();
    }

    protected OpenAIImpl(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
        setup();
    }

    public static OpenAIImpl getInstance() {
        return new OpenAIImpl();
    }

    public static OpenAIImpl getInstance(DBConnectionIFC inputDBConnection) {
        return new OpenAIImpl(inputDBConnection);
    }

    public static String o1 = "o1";
    public static String o1_preview = "o1-preview";
    public static String o1_mini = "o1-mini";
    public static String gpt_4o_mini = "gpt-4o-mini";
    public static String gpt_4o = "gpt-4o";
    public static String gpt_4_turbo_preview = "gpt-4-turbo-preview";
    public static String gpt_35_turbo = "gpt-3.5-turbo";
    public static String text_embedding_3_large = "text-embedding-3-large";
    public static String text_embedding_3_small = "text-embedding-3-small";
    public static String dall_e_3 = "dall-e-3";
    public static String dall_e_2 = "dall-e-2";
    public static String tts_1 = "tts-1";
    public static String tts_1_hd = "tts-1-hd";
    public static String whisper_1 = "whisper-1";

    private String[] chatModels;
    private String[] embeddingModels;
    private String[] imageModels;
    private String[] visionModels;
    private String[] textToSpeechModels;
    private String[] speechToTextModels;

    private Map<String, String> urlMapping;
    private Map<String, Integer> contextWindowMapping;
    private Map<String, Integer> maxOutputMapping;
    private Map<String, Boolean> supportSystemMapping;

    private void setup() {
        chatModels = new String[]{o1, o1_preview, o1_mini, gpt_4o_mini, gpt_4o, gpt_4_turbo_preview, gpt_35_turbo};
        embeddingModels = new String[]{text_embedding_3_large, text_embedding_3_small};
        imageModels = new String[]{dall_e_3, dall_e_2};
        visionModels = new String[]{o1, gpt_4o_mini, gpt_4o};
        textToSpeechModels = new String[]{tts_1_hd, tts_1};
        speechToTextModels = new String[]{whisper_1};

        urlMapping = new ConcurrentHashMap<String, String>();
        urlMapping.put(o1, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(o1_preview, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(o1_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_4o_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_4o, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_4_turbo_preview, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_35_turbo, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(text_embedding_3_large, "https://api.openai.com/v1/embeddings");
        urlMapping.put(text_embedding_3_small, "https://api.openai.com/v1/embeddings");
        urlMapping.put(dall_e_3, "https://api.openai.com/v1/images/generations");
        urlMapping.put(dall_e_2, "https://api.openai.com/v1/images/generations");
        urlMapping.put(tts_1, "https://api.openai.com/v1/audio/speech");
        urlMapping.put(tts_1_hd, "https://api.openai.com/v1/audio/speech");
        urlMapping.put(whisper_1, "https://api.openai.com/v1/audio/transcriptions");

        contextWindowMapping = new ConcurrentHashMap<String, Integer>();
        contextWindowMapping.put(o1, 200000);
        contextWindowMapping.put(o1_preview, 128000);
        contextWindowMapping.put(o1_mini, 128000);
        contextWindowMapping.put(gpt_4o_mini, 128000);
        contextWindowMapping.put(gpt_4o, 128000);
        contextWindowMapping.put(gpt_4_turbo_preview, 128000);
        contextWindowMapping.put(gpt_35_turbo, 16385);

        maxOutputMapping = new ConcurrentHashMap<String, Integer>();
        maxOutputMapping.put(o1, 100000);
        maxOutputMapping.put(o1_preview, 32768);
        maxOutputMapping.put(o1_mini, 65536);
        maxOutputMapping.put(gpt_4o_mini, 16384);
        maxOutputMapping.put(gpt_4o, 16384);
        maxOutputMapping.put(gpt_4_turbo_preview, 4096);
        maxOutputMapping.put(gpt_35_turbo, 4096);

        supportSystemMapping = new ConcurrentHashMap<String, Boolean>();
        supportSystemMapping.put(o1, false);
        supportSystemMapping.put(o1_preview, false);
        supportSystemMapping.put(o1_mini, false);
        supportSystemMapping.put(gpt_4o_mini, true);
        supportSystemMapping.put(gpt_4o, true);
        supportSystemMapping.put(gpt_4_turbo_preview, true);
        supportSystemMapping.put(gpt_35_turbo, true);
    }

    @Override
    protected String getApiKey() {
        try {
            if(dbConnection != null) {
                return CommonUtil.getConfigValue(dbConnection, "OpenAiApiKey");
            }
            else {
                return CommonUtil.getConfigValue("OpenAiApiKey");
            }
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
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
    public String[] getSpeechToTextModels() {
        return speechToTextModels;
    }

    @Override
    protected String getUrl(String model) {
        if(urlMapping.containsKey(model)) {
            return urlMapping.get(model);
        }
        else {
            throw new NeoAIException("model " + model + " not supported to get url!");
        }
    }

    @Override
    protected int getContextWindow(String model) {
        if(contextWindowMapping.containsKey(model)) {
            return contextWindowMapping.get(model);
        }
        else {
            throw new NeoAIException("model " + model + " not supported to get context window!");
        }
    }

    @Override
    protected int getMaxOutputTokenNumber(String model) {
        if(maxOutputMapping.containsKey(model)) {
            return maxOutputMapping.get(model);
        }
        else {
            throw new NeoAIException("model " + model + " not supported to get max output tokens!");
        }
    }

    @Override
    protected String getDefaultSystemHint() {
        return "You are a helpful assistant. You always response result in plain text.";
    }

    @Override
    protected boolean isSupportSystemHint(String model) {
        if(supportSystemMapping.containsKey(model)) {
            return supportSystemMapping.get(model);
        }
        else {
            throw new NeoAIException("model " + model + " not supported to get is support System!");
        }
    }
}
