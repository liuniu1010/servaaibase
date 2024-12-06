package org.neo.servaframe;

import java.util.*;
import java.io.*;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.neo.servaframe.util.*;
import org.neo.servaframe.interfaces.*;
import org.neo.servaframe.model.*;

import org.neo.servaaibase.*;
import org.neo.servaaibase.ifc.*;
import org.neo.servaaibase.impl.*;
import org.neo.servaaibase.model.*;

/**
 * Unit test 
 */
public class GoogleAIImplTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GoogleAIImplTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Code to set up resources or initialize variables before each test method
    }

    @Override
    protected void tearDown() throws Exception {
        // Code to clean up resources after each test method
        super.tearDown();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( GoogleAIImplTest.class );
    }

    private String[] getChatModels() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleAIImpl googleAI = GoogleAIImpl.getInstance(dbConnection);
                String[] models = googleAI.getChatModels();
                return models;
            }
        });
    }

    private void fetchChatResponse(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleAIImpl googleAI = GoogleAIImpl.getInstance(dbConnection);

                String[] models = googleAI.getChatModels();
                for(String model: models) {
                    printChat(model, userInput, googleAI);
                }
                return null; 
            }

            private void printChat(String model, String userInput, GoogleAIImpl googleAI) {
                System.out.println("test fetchChatResponse on model [" + model + "]");
                System.out.println("userInput = " + userInput);
                AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
                promptStruct.setUserInput(userInput);
                AIModel.ChatResponse chatResponse = googleAI.fetchChatResponse(model, promptStruct);
                System.out.println("response = " + chatResponse.getMessage());
            }
        });
    }

    private String visionImage(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeQueryTask(new GoogleVisionImageTask(userInput));
    }

    private AIModel.Embedding getEmbedding(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (AIModel.Embedding)dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleAIImpl googleAI = GoogleAIImpl.getInstance(dbConnection);
                String[] models = googleAI.getEmbeddingModels();
                for(String model: models) {
                    System.out.println("test getEmbedding on model [" + model + "]");
                    System.out.println("userInput = " + userInput);
                    AIModel.Embedding embedding = googleAI.getEmbedding(model, userInput);
                    System.out.println("embedding.size = " + embedding.size());
                    System.out.println("embedding = " + embedding.toString());
                }
                return null; 
            }
        });
    }

    private String[] generateImages(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                GoogleAIImpl googleAI = GoogleAIImpl.getInstance(dbConnection);
                String[] models = googleAI.getImageModels();
                for(String model: models) {
                    System.out.println("test generateImages on model [" + model + "]");
                    System.out.println("userInput = " + userInput);
                    AIModel.ImagePrompt imagePrompt = new AIModel.ImagePrompt();
                    imagePrompt.setUserInput(userInput);
                    String[] urls = googleAI.generateImages(models[0], imagePrompt);
                    for(String url: urls) {
                        System.out.println("url = " + url);
                    }
                }
                return null; 
            }
        });
    }

    public void _testGetChatModels() {
        String[] models = getChatModels();
        System.out.println("models.size = " + models.length);
        for(String model: models) {
            System.out.println("model = " + model);
        }
    }

    public void testFetchChatResponse() throws Exception {
        try {
            String userInput = "Hello, how are you! I'm Neo, nice to meet you!";
            fetchChatResponse(userInput);
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    public void testSystemHint() throws Exception {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            dbService.executeQueryTask(new DBQueryTaskIFC() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    GoogleAIImpl googleAI = GoogleAIImpl.getInstance(dbConnection);
                    String userInput1 = "Hello, how are you! I'm Neo, nice to meet you!";
                    String userInput2 = "这是一段语言测试";
                    String[] userInputs = new String[]{userInput1, userInput2};
                    String systemHint = "You are a great language expert, you never response user prompt with your own idea, what you need to do is just translating input prompt, in case the input is English, you translate it into Chinese, in case the input is Chinese, you translate it into English";
                    String[] models = googleAI.getChatModels();
                    for(String model: models) {
                        System.out.println("test fetchChatResponse with model [" + model + "]");
                        for(String userInput: userInputs) {
                            System.out.println("userInput = " + userInput);
                            AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
                            promptStruct.setUserInput(userInput);
                            promptStruct.setSystemHint(systemHint);
                            AIModel.ChatResponse chatResponse = googleAI.fetchChatResponse(model, promptStruct);
                            System.out.println("response = " + chatResponse.getMessage());
                        }
                    }
                    return null;
                }
            });
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    public void _testVisionImage() throws Exception {
        try {
            String userInput = "Hello, please give me an description of the images";
            visionImage(userInput);
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    public void testGetEmbedding() throws Exception {
        try {
            String userInput = "Hello, how are you! I'm Neo, nice to meet you!";
            getEmbedding(userInput);
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    public void _testGenerateImage() throws Exception {
        try {
            String userInput = "Blue sky outside the window, with white clouds and blue sea";
            generateImages(userInput);
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }
}

class GoogleVisionImageTask implements DBQueryTaskIFC {
    private String userInput;

    public GoogleVisionImageTask(String inputUserInput) {
        userInput = inputUserInput;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        try {
            return innerQuery(dbConnection);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Object innerQuery(DBConnectionIFC dbConnection) throws Exception {
        GoogleAIImpl googleAI = GoogleAIImpl.getInstance(dbConnection);
        String[] models = googleAI.getVisionModels();
        for(String model: models) {
            System.out.println("test vision on model [" + model + "]");
            System.out.println("userInput = " + userInput);
            AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
            promptStruct.setUserInput(userInput);

            AIModel.Attachment attachment1 = new AIModel.Attachment();
            InputStream in1 = new FileInputStream("/tmp/dogandcat.png");
            String rawBase64OfAttach1 = IOUtil.inputStreamToRawBase64(in1);
            String base64 = "data:image/png;base64," + rawBase64OfAttach1;
            attachment1.setContent(base64);
/*
            AIModel.Attachment attachment2 = new AIModel.Attachment();
            InputStream in2 = new FileInputStream("/tmp/image.jpg");
            String rawBase64OfAttach2 = IOUtil.inputStreamToRawBase64(in2);
            base64 = "data:image/jpeg;base64," + rawBase64OfAttach2;
            attachment2.setContent(base64);
*/
            AIModel.Attachment attachment3 = new AIModel.Attachment();
            String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg";
            attachment3.setContent(imageUrl);

            List<AIModel.Attachment> attachments = new ArrayList<AIModel.Attachment>();
            attachments.add(attachment1);
            // attachments.add(attachment2);
            // attachments.add(attachment3);

            AIModel.AttachmentGroup attachmentGroup = new AIModel.AttachmentGroup();
            attachmentGroup.setAttachments(attachments);

            promptStruct.setAttachmentGroup(attachmentGroup);

            AIModel.ChatResponse chatResponse = googleAI.fetchChatResponse(model, promptStruct);
            System.out.println("response = " + chatResponse.getMessage());
        }
        return null; 
    }
}

