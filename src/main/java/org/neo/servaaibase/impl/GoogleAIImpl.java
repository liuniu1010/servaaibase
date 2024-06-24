package org.neo.servaaibase.impl;

import java.util.Map;
import java.util.HashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

public class GoogleAIImpl extends AbsGoogleAIImpl {
    private DBConnectionIFC dbConnection;

    protected GoogleAIImpl() {
        setup();
    }

    protected GoogleAIImpl(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
        setup();
    }

    public static GoogleAIImpl getInstance() {
        return new GoogleAIImpl();
    }

    public static GoogleAIImpl getInstance(DBConnectionIFC inputDBConnection) {
        return new GoogleAIImpl(inputDBConnection);
    }

    public static String gemini_1_0_pro = "gemini-1.0-pro";
    public static String gemini_1_5_pro_latest = "gemini-1.5-pro-latest";
    public static String embedding_001 = "embedding-001";
    public static String gemini_pro_vision = "gemini-pro-vision";

    private String[] chatModels;
    private String[] embeddingModels;
    private String[] imageModels;
    private String[] visionModels;
    private String[] textToSpeechModels;
    private String[] speechToTextModels;

    private Map<String, Integer> maxOutputMapping;
    private Map<String, Integer> maxInputMapping;

    private void setup() {
        chatModels = new String[]{gemini_1_5_pro_latest, gemini_1_0_pro};
        embeddingModels = new String[]{embedding_001};
        imageModels = new String[]{};
        visionModels = new String[]{gemini_pro_vision};
        textToSpeechModels = new String[] {};
        speechToTextModels = new String[] {}; 

        maxOutputMapping = new HashMap<String, Integer>();
        maxOutputMapping.put(gemini_1_5_pro_latest, 8192);
        maxOutputMapping.put(gemini_1_0_pro, 2048);
        maxOutputMapping.put(gemini_pro_vision, 4096);

        maxInputMapping = new HashMap<String, Integer>();
        maxInputMapping.put(gemini_1_5_pro_latest, 1048576);
        maxInputMapping.put(gemini_1_0_pro, 30720);
        maxInputMapping.put(gemini_pro_vision, 12288);
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
    protected String getUrl(String model, String action) {
        String apiKey = getApiKey();
        return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":" + action + "?key=" + apiKey;
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
    protected int getMaxInputTokenNumber(String model) {
        if(maxInputMapping.containsKey(model)) {
            return maxInputMapping.get(model);
        }
        else {
            throw new NeoAIException("model " + model + " not supported to get max input tokens!");
        }
    }
}
