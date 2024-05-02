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
public class OpenAIImplTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OpenAIImplTest( String testName ) {
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
        return new TestSuite( OpenAIImplTest.class );
    }

    private String[] getChatModels() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeQueryTask(new GetModelTask());
    }

    private String fetchChatResponse(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeQueryTask(new FetchChatResponseTask(userInput));
    }

    private String visionImage(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeQueryTask(new VisionImageTask(userInput));
    }

    private AIModel.Embedding getEmbedding(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (AIModel.Embedding)dbService.executeQueryTask(new GetEmbeddingTask(userInput));
    }

    private String[] generateImages(String userInput) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeQueryTask(new GenerateImageTask(userInput));
    }

    public void testGetChatModels() {
        String[] models = getChatModels();
        System.out.println("models.size = " + models.length);
        for(String model: models) {
            System.out.println("model = " + model);
        }
    }

    public void testFetchChatResponse() throws Exception {
        try {
            String userInput = "Hello, how are you! I'm Neo, nice to meet you!";
            String response = fetchChatResponse(userInput);
            System.out.println("userInput = " + userInput);
            System.out.println("response = " + response);
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    public void testVisionImage() throws Exception {
        try {
            String userInput = "Hello, please give me an description of the images";
            String response = visionImage(userInput);
            System.out.println("userInput = " + userInput);
            System.out.println("response = " + response);
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    public void testGetEmbedding() throws Exception {
        try {
            String userInput = "Hello, how are you! I'm Neo, nice to meet you!";
            AIModel.Embedding embedding = getEmbedding(userInput);
            System.out.println("userInput = " + userInput);
            System.out.println("embedding.size = " + embedding.size());
            System.out.println("embedding = " + embedding.toString());
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }

    public void testGenerateImage() throws Exception {
        try {
            String userInput = "Blue sky outside the window, with white clouds and blue sea";
            String[] urls = generateImages(userInput);
            for(String url: urls) {
                System.out.println("image url = " + url);
            }
        }
        catch(Exception ex) {
            System.out.println("ex.message = " + ex.getMessage());
            throw ex;
        }
    }
}

class GetModelTask implements DBQueryTaskIFC {
    @Override
    public Object query(DBConnectionIFC dbConnection) {
        OpenAIImpl openAI = OpenAIImpl.getInstance(dbConnection);
        String[] models = openAI.getChatModels();
        return models;
    }
}

class FetchChatResponseTask implements DBQueryTaskIFC {
    private String userInput;

    public FetchChatResponseTask(String inputUserInput) {
        userInput = inputUserInput;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        OpenAIImpl openAI = OpenAIImpl.getInstance(dbConnection);
        String[] models = openAI.getChatModels();
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        promptStruct.setUserInput(userInput);
        AIModel.ChatResponse chatResponse = openAI.fetchChatResponse(models[0], promptStruct);
        return chatResponse.getMessage(); 
    }
}

class VisionImageTask implements DBQueryTaskIFC {
    private String userInput;

    public VisionImageTask(String inputUserInput) {
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
        OpenAIImpl openAI = OpenAIImpl.getInstance(dbConnection);
        String[] models = openAI.getVisionModels();
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        promptStruct.setUserInput(userInput);

        AIModel.Attachment attachment1 = new AIModel.Attachment();
        InputStream in = new FileInputStream("/tmp/dogandcat.png");
        String rawBase64 = IOUtil.inputStreamToRawBase64(in);
        String base64 = "data:image/png;base64," + rawBase64;
        attachment1.setContent(base64);

        AIModel.Attachment attachment2 = new AIModel.Attachment();
        attachment2.setContent("https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");

        List<AIModel.Attachment> attachments = new ArrayList<AIModel.Attachment>();
        attachments.add(attachment1);
        attachments.add(attachment2);

        AIModel.AttachmentGroup attachmentGroup = new AIModel.AttachmentGroup();
        attachmentGroup.setAttachments(attachments);

        promptStruct.setAttachmentGroup(attachmentGroup);

        AIModel.ChatResponse chatResponse = openAI.fetchChatResponse(models[0], promptStruct);
        return chatResponse.getMessage(); 
    }
}

class GetEmbeddingTask implements DBQueryTaskIFC {
    private String userInput;

    public GetEmbeddingTask(String inputUserInput) {
        userInput = inputUserInput;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        OpenAIImpl openAI = OpenAIImpl.getInstance(dbConnection);
        String[] models = openAI.getEmbeddingModels();
        AIModel.Embedding embedding = openAI.getEmbedding(models[0], userInput, 12);
        return embedding; 
    }
}

class GenerateImageTask implements DBQueryTaskIFC {
    private String userInput;

    public GenerateImageTask(String inputUserInput) {
        userInput = inputUserInput;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        OpenAIImpl openAI = OpenAIImpl.getInstance(dbConnection);
        String[] models = openAI.getImageModels();
        AIModel.ImagePrompt imagePrompt = new AIModel.ImagePrompt();
        imagePrompt.setUserInput(userInput);
        String[] urls = openAI.generateImages(models[0], imagePrompt);
        return urls; 
    }
}
