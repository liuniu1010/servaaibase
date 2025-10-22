package org.neo.servaframe;

import java.util.*;
import java.io.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.neo.servaframe.util.*;
import org.neo.servaframe.interfaces.*;
import org.neo.servaframe.model.*;

import org.neo.servaaibase.*;
import org.neo.servaaibase.ifc.*;
import org.neo.servaaibase.impl.*;
import org.neo.servaaibase.model.*;

/**
 * Unit tests (JUnit 5 / Java 21) for GoogleImpl integration.
 * Logic mirrors the OpenAI suite for parity.
 */
public class GoogleImplTest {

    @BeforeEach
    void setUp() throws Exception {
        // Code to set up resources or initialize variables before each test method
    }

    @AfterEach
    void tearDown() throws Exception {
        // Code to clean up resources after each test method
    }

    private String[] getChatModels() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[]) dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleImpl google = GoogleImpl.getInstance(dbConnection);
                return google.getChatModels();
            }
        });
    }

    private void fetchChatResponse(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleImpl google = GoogleImpl.getInstance(dbConnection);

                String[] models = google.getChatModels();
                for (String model : models) {
                    printChat(model, userInput, google);
                }
                return null;
            }

            private void printChat(String model, String userInput, GoogleImpl google) {
                System.out.println("test fetchChatResponse on model [" + model + "]");
                System.out.println("userInput = " + userInput);
                AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
                promptStruct.setUserInput(userInput);
                AIModel.ChatResponse chatResponse = google.fetchChatResponse(model, promptStruct);
                System.out.println("response = " + chatResponse.getMessage());
            }
        });
    }

    private String visionImage(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String) dbService.executeQueryTask(new GoogleVisionImageTask(userInput));
    }

    private AIModel.Embedding getEmbedding(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (AIModel.Embedding) dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleImpl google = GoogleImpl.getInstance(dbConnection);
                String[] models = google.getEmbeddingModels();
                for (String model : models) {
                    System.out.println("test getEmbedding on model [" + model + "]");
                    System.out.println("userInput = " + userInput);
                    AIModel.Embedding embedding = google.getEmbedding(model, userInput);
                    System.out.println("embedding.size = " + embedding.size());
                    System.out.println("embedding = " + embedding.toString());
                }
                return null;
            }
        });
    }

    private String[] generateImages(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[]) dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleImpl google = GoogleImpl.getInstance(dbConnection);
                String[] models = google.getImageModels();
                for (String model : models) {
                    System.out.println("test generateImages on model [" + model + "]");
                    System.out.println("userInput = " + userInput);
                    AIModel.ImagePrompt imagePrompt = new AIModel.ImagePrompt();
                    imagePrompt.setUserInput(userInput);
                    String[] urls = google.generateImages(model, imagePrompt);
                    if (urls != null) {
                        for (String url : urls) {
                            System.out.println("url = " + url);
                        }
                    }
                }
                return null;
            }
        });
    }

    @Test
    void testGetChatModels() {
        String[] models = getChatModels();
        System.out.println("models.size = " + models.length);
        for (String model : models) {
            System.out.println("model = " + model);
        }
    }

    @Test
    void testFetchChatResponse() throws Exception {
        try {
            String userInput = "Hello, how are you! I'm Neo, nice to meet you!";
            fetchChatResponse(userInput);
        } catch (Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    @Test
    void testSystemHint() throws Exception {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            dbService.executeQueryTask(new DBQueryTaskIFC() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    GoogleImpl google = GoogleImpl.getInstance(dbConnection);
                    String userInput1 = "Hello, how are you! I'm Neo, nice to meet you!";
                    String userInput2 = "这是一段语言测试";
                    String[] userInputs = new String[]{userInput1, userInput2};
                    String systemHint = "You are a great language expert, you never response user prompt with your own idea, what you need to do is just translating input prompt, in case the input is English, you translate it into Chinese, in case the input is Chinese, you translate it into English";
                    String[] models = google.getChatModels();
                    for (String model : models) {
                        System.out.println("test fetchChatResponse with model [" + model + "]");
                        for (String userInput : userInputs) {
                            System.out.println("userInput = " + userInput);
                            AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
                            promptStruct.setUserInput(userInput);
                            promptStruct.setSystemHint(systemHint);
                            AIModel.ChatResponse chatResponse = google.fetchChatResponse(model, promptStruct);
                            System.out.println("response = " + chatResponse.getMessage());
                        }
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    @Test
    void testVisionImage() throws Exception {
        try {
            String userInput = "Hello, please give me a description of the images";
            visionImage(userInput);
        } catch (Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    @Test
    void testGetEmbedding() throws Exception {
        try {
            String userInput = "Hello, how are you! I'm Neo, nice to meet you!";
            getEmbedding(userInput);
        } catch (Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    @Test
    void testGenerateImage() throws Exception {
        try {
            String userInput = "Blue sky outside the window, with white clouds and blue sea";
            generateImages(userInput);
        } catch (Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }
}

class GoogleVisionImageTask implements DBQueryTaskIFC {
    private final String userInput;

    public GoogleVisionImageTask(String inputUserInput) {
        userInput = inputUserInput;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        try {
            return innerQuery(dbConnection);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Object innerQuery(DBConnectionIFC dbConnection) throws Exception {
        GoogleImpl google = GoogleImpl.getInstance(dbConnection);
        String[] models = google.getVisionModels();
        for (String model : models) {
            System.out.println("test vision on model [" + model + "]");
            System.out.println("userInput = " + userInput);
            AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
            promptStruct.setUserInput(userInput);

            AIModel.Attachment attachment1 = new AIModel.Attachment();
            String rawBase64 = IOUtil.resourceFileToRawBase64("dogandcat.png");
            String base64 = "data:image/png;base64," + rawBase64;
            attachment1.setContent(base64);

            List<AIModel.Attachment> attachments = new ArrayList<AIModel.Attachment>();
            attachments.add(attachment1);

            AIModel.AttachmentGroup attachmentGroup = new AIModel.AttachmentGroup();
            attachmentGroup.setAttachments(attachments);

            promptStruct.setAttachmentGroup(attachmentGroup);

            AIModel.ChatResponse chatResponse = google.fetchChatResponse(model, promptStruct);
            System.out.println("response = " + chatResponse.getMessage());
        }
        return null;
    }
}
