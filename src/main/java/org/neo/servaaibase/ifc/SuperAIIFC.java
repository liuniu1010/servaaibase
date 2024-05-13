package org.neo.servaaibase.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaibase.model.AIModel;

public interface SuperAIIFC {
    public String[] getChatModels();
    public String[] getEmbeddingModels();
    public String[] getImageModels();
    public String[] getVisionModels();
    public String[] getTextToSpeechModels();
    public String[] getSpeechToTextModels();
    public AIModel.ChatResponse fetchChatResponse(String inputModel, AIModel.PromptStruct inputPromptStruct);
    public AIModel.ChatResponse fetchChatResponse(String inputModel, AIModel.PromptStruct inputPromptStruct, FunctionCallIFC functionCallIFC);
    public AIModel.Embedding getEmbedding(String model, String input);
    public AIModel.Embedding getEmbedding(String model, String input, int dimensions);
    public String[] generateImages(String model, AIModel.ImagePrompt imagePrompt);
    public String generateSpeech(String model, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileAbsolutePath);
    public AIModel.ChatResponse speechToText(String model, AIModel.Attachment attachment);
}
