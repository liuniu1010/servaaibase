package org.neo.servaaibase.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

public class GoogleImpl extends AbsGoogleImpl {
    protected DBConnectionIFC dbConnection;

    protected GoogleImpl() {
        setup();
    }

    protected GoogleImpl(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
        setup();
    }

    public static GoogleImpl getInstance() {
        return new GoogleImpl();
    }

    public static GoogleImpl getInstance(DBConnectionIFC inputDBConnection) {
        return new GoogleImpl(inputDBConnection);
    }

    public static String gemini_25_pro = "gemini-2.5-pro";
    public static String gemini_25_flash = "gemini-2.5-flash";
    public static String gemini_25_flash_lite = "gemini-2.5-flash-lite";
    public static String gemini_20_flash = "gemini-2.0-flash";
    public static String gemini_20_flash_lite = "gemini-2.0-flash-lite";

    // public static String gemini_20_flash = "gemini-2.0-flash-exp";
    // public static String gemini_20_pro = "gemini-2.0-pro-exp";
    public static String gemini_15_pro = "gemini-1.5-pro-latest";
    public static String gemini_15_flash = "gemini-1.5-flash-latest";
    public static String gemini_15_flash_8b = "gemini-1.5-flash-8b-latest";
    public static String gemini_10_pro = "gemini-1.0-pro";
    public static String gemini_10_vision = "gemini-1.0-pro-vision";
    public static String text_embedding_004 = "text-embedding-004";
    public static String imagen_30 = "imagen-3.0-generate";
    public static String imagen_30_lite = "imagen-3.0-lite";
    public static String tts_standard = "google-tts-standard";
    public static String tts_neural = "google-tts-neural";
    public static String speech_standard = "google-speech-standard";

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
        chatModels = new String[]{gemini_25_pro, gemini_25_flash, gemini_25_flash_lite, gemini_20_flash, gemini_20_flash_lite};

        embeddingModels = new String[]{text_embedding_004};
        imageModels = new String[]{imagen_30, imagen_30_lite};
        visionModels = new String[]{gemini_20_flash, /*gemini_20_pro,*/ gemini_15_pro, gemini_15_flash, gemini_10_vision};
        textToSpeechModels = new String[]{tts_neural, tts_standard};
        speechToTextModels = new String[]{speech_standard};

        urlMapping = new ConcurrentHashMap<String, String>();
        urlMapping.put(gemini_25_pro, buildGenerateContentUrl(gemini_25_pro));
        urlMapping.put(gemini_25_flash, buildGenerateContentUrl(gemini_25_flash));
        urlMapping.put(gemini_25_flash_lite, buildGenerateContentUrl(gemini_25_flash_lite));
        urlMapping.put(gemini_20_flash, buildGenerateContentUrl(gemini_20_flash));
        urlMapping.put(gemini_20_flash_lite, buildGenerateContentUrl(gemini_20_flash_lite));

        //urlMapping.put(gemini_20_flash, buildGenerateContentUrl(gemini_20_flash));
        // urlMapping.put(gemini_20_pro, buildGenerateContentUrl(gemini_20_pro));
        urlMapping.put(gemini_15_pro, buildGenerateContentUrl(gemini_15_pro));
        urlMapping.put(gemini_15_flash, buildGenerateContentUrl(gemini_15_flash));
        urlMapping.put(gemini_15_flash_8b, buildGenerateContentUrl(gemini_15_flash_8b));
        urlMapping.put(gemini_10_pro, buildGenerateContentUrl(gemini_10_pro));
        urlMapping.put(gemini_10_vision, buildGenerateContentUrl(gemini_10_vision));
        urlMapping.put(text_embedding_004, buildEmbedContentUrl(text_embedding_004));
        urlMapping.put(imagen_30, buildGenerateImageUrl(imagen_30));
        urlMapping.put(imagen_30_lite, buildGenerateImageUrl(imagen_30_lite));
        urlMapping.put(tts_neural, "https://texttospeech.googleapis.com/v1/text:synthesize");
        urlMapping.put(tts_standard, "https://texttospeech.googleapis.com/v1/text:synthesize");
        urlMapping.put(speech_standard, "https://speech.googleapis.com/v1/speech:recognize");

        contextWindowMapping = new ConcurrentHashMap<String, Integer>();
        contextWindowMapping.put(gemini_25_pro, 1048576);
        contextWindowMapping.put(gemini_25_flash, 1048576);
        contextWindowMapping.put(gemini_25_flash_lite, 1048576);
        contextWindowMapping.put(gemini_20_flash, 1048576);
        contextWindowMapping.put(gemini_20_flash_lite, 1048576);

        // contextWindowMapping.put(gemini_20_flash, 1000000);
        contextWindowMapping.put(gemini_15_pro, 2000000);
        contextWindowMapping.put(gemini_15_flash, 1000000);
        contextWindowMapping.put(gemini_15_flash_8b, 1000000);
        contextWindowMapping.put(gemini_10_pro, 32000);
        contextWindowMapping.put(gemini_10_vision, 32000);

        maxOutputMapping = new ConcurrentHashMap<String, Integer>();
        maxOutputMapping.put(gemini_25_pro, 65536);
        maxOutputMapping.put(gemini_25_flash, 65536);
        maxOutputMapping.put(gemini_25_flash_lite, 65536);
        maxOutputMapping.put(gemini_20_flash, 8192);
        maxOutputMapping.put(gemini_20_flash_lite, 8192);

        // maxOutputMapping.put(gemini_20_flash, 8192);
        maxOutputMapping.put(gemini_15_pro, 8192);
        maxOutputMapping.put(gemini_15_flash, 8192);
        maxOutputMapping.put(gemini_15_flash_8b, 8192);
        maxOutputMapping.put(gemini_10_pro, 4096);
        maxOutputMapping.put(gemini_10_vision, 4096);

        supportSystemMapping = new ConcurrentHashMap<String, Boolean>();
        supportSystemMapping.put(gemini_25_pro, true);
        supportSystemMapping.put(gemini_25_flash, true);
        supportSystemMapping.put(gemini_25_flash_lite, true);
        supportSystemMapping.put(gemini_20_flash, true);
        supportSystemMapping.put(gemini_20_flash_lite, true);

        // supportSystemMapping.put(gemini_20_flash, true);
        supportSystemMapping.put(gemini_15_pro, true);
        supportSystemMapping.put(gemini_15_flash, true);
        supportSystemMapping.put(gemini_15_flash_8b, true);
        supportSystemMapping.put(gemini_10_pro, true);
        supportSystemMapping.put(gemini_10_vision, true);
    }

    private String buildGenerateContentUrl(String model) {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
    }

    private String buildEmbedContentUrl(String model) {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":embedContent";
    }

    private String buildGenerateImageUrl(String model) {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateImage";
    }

    @Override
    protected String getApiKey() {
        try {
            if(dbConnection != null) {
                return CommonUtil.getConfigValue(dbConnection, "GoogleApiKey");
            }
            else {
                return CommonUtil.getConfigValue("GoogleApiKey");
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
        return "You are a helpful Gemini assistant. You respond in concise plain text.";
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
