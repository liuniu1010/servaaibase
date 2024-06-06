package org.neo.servaframe;

import java.util.*;
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
import org.neo.servaaibase.util.*;

/**
 * Unit test 
 */
public class CommonUtilTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CommonUtilTest( String testName ) {
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
        return new TestSuite( CommonUtilTest.class );
    }

    public void testRandomChooseOpenAiApiKey() throws Exception {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new DBQueryTaskIFC() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                for(int i = 0;i < 5;i++) {
                    String randomChoosedKey = CommonUtil.randomChooseOpenAiApiKey(dbConnection);
                    System.out.println("randomChoosedKey = " + randomChoosedKey);
                }
                return null;
            }
        });
    }

    public void testCommand() throws Exception {
        // String command = "/bin/sh -c \"ls -l | grep liuniu\"";
        String command = "java -version";
        System.out.println("command = " + command);
        try {
            String result = CommonUtil.executeCommand(command);
            System.out.println("execute success, result = " + result);
        }
        catch(Exception ex) {
            System.out.println("execute fail, message = " + ex.getMessage());
        }
    }

    public void testMimeTypeBase64() {
        String rawBase64 = "iVBORw0KGgoA";
        String mimeType = "image/png";
        String prefix = "data:" + mimeType + ";base64,";
        String base64 = prefix + rawBase64;

        String extractMimeType = CommonUtil.extractMimeTypeFromBase64(base64);
        String extractRawBase64 = CommonUtil.extractRawBase64(base64);
        assertEquals(extractMimeType, mimeType);
        assertEquals(extractRawBase64, rawBase64);

        extractMimeType = CommonUtil.extractMimeTypeFromBase64(rawBase64);
        extractRawBase64 = CommonUtil.extractRawBase64(rawBase64);
        assertEquals(extractMimeType, null);
        assertEquals(extractRawBase64, rawBase64);


        mimeType = "image/jpeg";
        prefix = "data:" + mimeType + ";base64,";
        base64 = prefix + rawBase64;

        extractMimeType = CommonUtil.extractMimeTypeFromBase64(base64);
        extractRawBase64 = CommonUtil.extractRawBase64(base64);
        assertEquals(extractMimeType, mimeType);
        assertEquals(extractRawBase64, rawBase64);

        extractMimeType = CommonUtil.extractMimeTypeFromBase64(rawBase64);
        extractRawBase64 = CommonUtil.extractRawBase64(rawBase64);
        assertEquals(extractMimeType, null);
        assertEquals(extractRawBase64, rawBase64);
    }

    public void testGetRandomString() {
        int length = 25;
        String randomString = CommonUtil.getRandomString(length);
        System.out.println("randomString = " + randomString);
        System.out.println("randomString size = " + randomString.length());

        assertEquals(randomString.length(), length);
    }

    public void testNormalizeFolderPath() {
        String folderPath1 = "/home/neo/";
        String expectedPath1 = "/home/neo";

        String folderPath2 = "/tmp";
        String expectedPath2 = "/tmp";

        String folderPath3 = "/";
        String expectedPath3 = "/";

        String normalizedPath1 = CommonUtil.normalizeFolderPath(folderPath1);
        String normalizedPath2 = CommonUtil.normalizeFolderPath(folderPath2);
        String normalizedPath3 = CommonUtil.normalizeFolderPath(folderPath3);

        System.out.println("Normalized Path 1: " + normalizedPath1);
        System.out.println("Normalized Path 2: " + normalizedPath2);
        System.out.println("Normalized Path 3: " + normalizedPath3);

        assertEquals(normalizedPath1, expectedPath1);
        assertEquals(normalizedPath2, expectedPath2);
        assertEquals(normalizedPath3, expectedPath3);
    }

    public void testGetFileName() {
        String filePath = "/tmp/audio.mp3";
        String fileName = CommonUtil.getFileName(filePath);
        assertEquals(fileName, "audio.mp3");
    }
}

