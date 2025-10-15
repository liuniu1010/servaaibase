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

    public static String gpt_5 = "gpt-5";
    public static String gpt_5_mini = "gpt-5-mini";
    public static String gpt_5_nano = "gpt-5-nano";
    public static String gpt_5_chat = "gpt-5-chat-latest";
    public static String gpt_oss_120b = "gpt-oss-120b";
    public static String gpt_oss_20b = "gpt-oss-20b";
    public static String gpt_41 = "gpt-4.1";
    public static String gpt_41_mini = "gpt-4.1-mini";
    public static String gpt_41_nano = "gpt-4.1-nano";
    public static String o4_mini = "o4-mini";
    public static String o3 = "o3";
    public static String o3_mini = "o3-mini";
    public static String o1 = "o1";
    public static String o1_mini = "o1-mini";
    public static String gpt_4o_mini = "gpt-4o-mini";
    public static String gpt_4o_mini_tts = "gpt-4o-mini-tts";
    public static String gpt_4o_mini_transcribe = "gpt-4o-mini-transcribe";
    public static String gpt_4o_transcribe = "gpt-4o-transcribe";
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
        chatModels = new String[]{gpt_5, gpt_5_mini, gpt_5_nano, gpt_5_chat, gpt_41, gpt_41_mini, gpt_41_nano, o4_mini, o3, o3_mini, o1, o1_mini, gpt_4o_mini, gpt_4o, gpt_4_turbo_preview, gpt_35_turbo};
        embeddingModels = new String[]{text_embedding_3_large, text_embedding_3_small};
        imageModels = new String[]{dall_e_3, dall_e_2};
        visionModels = new String[]{gpt_5, gpt_5_mini, gpt_5_nano, gpt_5_chat, o3, o1, gpt_4o_mini, gpt_4o};
        textToSpeechModels = new String[]{gpt_4o_mini_tts, tts_1_hd, tts_1};
        speechToTextModels = new String[]{gpt_4o_transcribe, gpt_4o_mini_transcribe, whisper_1};

        urlMapping = new ConcurrentHashMap<String, String>();
        urlMapping.put(gpt_5, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_5_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_5_nano, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_5_chat, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_oss_120b, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_oss_20b, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_41, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_41_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_41_nano, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(o4_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(o3, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(o3_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(o1, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(o1_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_4o_mini, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_4o, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_4_turbo_preview, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(gpt_35_turbo, "https://api.openai.com/v1/chat/completions");
        urlMapping.put(text_embedding_3_large, "https://api.openai.com/v1/embeddings");
        urlMapping.put(text_embedding_3_small, "https://api.openai.com/v1/embeddings");
        urlMapping.put(dall_e_3, "https://api.openai.com/v1/images/generations");
        urlMapping.put(dall_e_2, "https://api.openai.com/v1/images/generations");
        urlMapping.put(gpt_4o_mini_tts, "https://api.openai.com/v1/audio/speech");
        urlMapping.put(tts_1, "https://api.openai.com/v1/audio/speech");
        urlMapping.put(tts_1_hd, "https://api.openai.com/v1/audio/speech");
        urlMapping.put(gpt_4o_transcribe, "https://api.openai.com/v1/audio/transcriptions");
        urlMapping.put(gpt_4o_mini_transcribe, "https://api.openai.com/v1/audio/transcriptions");
        urlMapping.put(whisper_1, "https://api.openai.com/v1/audio/transcriptions");

        contextWindowMapping = new ConcurrentHashMap<String, Integer>();
        contextWindowMapping.put(gpt_5, 400000);
        contextWindowMapping.put(gpt_5_mini, 400000);
        contextWindowMapping.put(gpt_5_nano, 400000);
        contextWindowMapping.put(gpt_5_chat, 128000);
        contextWindowMapping.put(gpt_oss_120b, 131072);
        contextWindowMapping.put(gpt_oss_20b, 131072);
        contextWindowMapping.put(gpt_41, 1047576);
        contextWindowMapping.put(gpt_41_mini, 1047576);
        contextWindowMapping.put(gpt_41_nano, 1047576);
        contextWindowMapping.put(o4_mini, 200000);
        contextWindowMapping.put(o3, 200000);
        contextWindowMapping.put(o3_mini, 200000);
        contextWindowMapping.put(o1, 200000);
        contextWindowMapping.put(o1_mini, 128000);
        contextWindowMapping.put(gpt_4o_mini, 128000);
        contextWindowMapping.put(gpt_4o, 128000);
        contextWindowMapping.put(gpt_4_turbo_preview, 128000);
        contextWindowMapping.put(gpt_35_turbo, 16385);

        maxOutputMapping = new ConcurrentHashMap<String, Integer>();
        maxOutputMapping.put(gpt_5, 128000);
        maxOutputMapping.put(gpt_5_mini, 128000);
        maxOutputMapping.put(gpt_5_nano, 128000);
        maxOutputMapping.put(gpt_5_chat, 16384);
        maxOutputMapping.put(gpt_oss_120b, 131072);
        maxOutputMapping.put(gpt_oss_20b, 131072);
        maxOutputMapping.put(gpt_41, 32768);
        maxOutputMapping.put(gpt_41_mini, 32768);
        maxOutputMapping.put(gpt_41_nano, 32768);
        maxOutputMapping.put(o4_mini, 100000);
        maxOutputMapping.put(o3, 100000);
        maxOutputMapping.put(o3_mini, 100000);
        maxOutputMapping.put(o1, 100000);
        maxOutputMapping.put(o1_mini, 65536);
        maxOutputMapping.put(gpt_4o_mini, 16384);
        maxOutputMapping.put(gpt_4o, 16384);
        maxOutputMapping.put(gpt_4_turbo_preview, 4096);
        maxOutputMapping.put(gpt_35_turbo, 4096);

        supportSystemMapping = new ConcurrentHashMap<String, Boolean>();
        supportSystemMapping.put(gpt_5, true);
        supportSystemMapping.put(gpt_5_mini, true);
        supportSystemMapping.put(gpt_5_nano, true);
        supportSystemMapping.put(gpt_5_chat, true);
        supportSystemMapping.put(gpt_oss_120b, true);
        supportSystemMapping.put(gpt_oss_20b, true);
        supportSystemMapping.put(gpt_41, true);
        supportSystemMapping.put(gpt_41_mini, true);
        supportSystemMapping.put(gpt_41_nano, true);
        supportSystemMapping.put(o4_mini, true);
        supportSystemMapping.put(o3, true);
        supportSystemMapping.put(o3_mini, true);
        supportSystemMapping.put(o1, true);
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
