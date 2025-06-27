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

    public void testTruncateText() {
        String str1 = "abcdefg";
        String str2 = "abcdefghasifhasjfa;fja";
        String str3 = "这是一";
        String str4 = "这是一个测试语句";

        System.out.println("truncate from start");
        System.out.println("str1 = " + str1);
        System.out.println("truncate str1 = " + CommonUtil.truncateTextFromStart(str1, 10));
        System.out.println("str2 = " + str2);
        System.out.println("truncate str2 = " + CommonUtil.truncateTextFromStart(str2, 10));
        System.out.println("str3 = " + str3);
        System.out.println("truncate str3 = " + CommonUtil.truncateTextFromStart(str3, 10));
        System.out.println("str4 = " + str4);
        System.out.println("truncate str4 = " + CommonUtil.truncateTextFromStart(str4, 10));

        System.out.println("\ntruncate from end");
        System.out.println("str1 = " + str1);
        System.out.println("truncate str1 = " + CommonUtil.truncateTextFromEnd(str1, 10));
        System.out.println("str2 = " + str2);
        System.out.println("truncate str2 = " + CommonUtil.truncateTextFromEnd(str2, 10));
        System.out.println("str3 = " + str3);
        System.out.println("truncate str3 = " + CommonUtil.truncateTextFromEnd(str3, 10));
        System.out.println("str4 = " + str4);
        System.out.println("truncate str4 = " + CommonUtil.truncateTextFromEnd(str4, 10));
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

    public void testGetConfigValues() throws Exception {
        String[] configNames = new String[]{"consumedCreditsOnCoderBot"
                                           ,"consumedCreditsOnSpeechSplit"
                                           ,"consumedCreditsOnUtilityBot"
                                           ,"consumedCreditsOnChatWithAssistant"
                                           ,"consumedCreditsOnSpeechToText"
                                           ,"paymentLinkOnStripe"
                                           ,"topupOnRegister"};
        Map<String, String> configMap = CommonUtil.getConfigValues(configNames);
        for(String configName: configNames) {
            String configValue = configMap.get(configName);
            System.out.println(configName + ": " + configValue);
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

    public void _testGetFileName() {
        String filePath = "/tmp/audio.mp3";
        String fileName = CommonUtil.getFileName(filePath);
        assertEquals(fileName, "audio.mp3");
    }

    public void testSaltHash() {
        String input = "someplaintext";
        
        String saltHash1 = CommonUtil.getSaltedHash(input);
        String saltHash2 = CommonUtil.getSaltedHash(input);
        System.out.println("input = " + input); 
        System.out.println("saltHash1 = " + saltHash1); 
        System.out.println("saltHash2 = " + saltHash2);

        System.out.println("saltHash1.length = " + saltHash1.length());

        boolean check1 = CommonUtil.checkPassword(input, saltHash1);
        boolean check2 = CommonUtil.checkPassword(input, saltHash2);
        boolean check3 = CommonUtil.checkPassword(input + "s", saltHash2);

        assertTrue(check1);
        assertTrue(check2);
        assertFalse(check3);
    }

    public void testEmailAddress() {
        String[] testEmails = {
            "test@example.com",
            "invalid-email",
            "another.test@domain.co",
            "user@domaincom",
            "user@domain.c"
        };

        for (String email : testEmails) {
            System.out.println(email + ": " + CommonUtil.isValidEmail(email));
        }
    }

    public void testAddTimeSpan() {
        Date date = new Date();
        Date date1 = CommonUtil.addTimeSpan(date, Calendar.MINUTE, 30);
        Date date2 = CommonUtil.addTimeSpan(date, Calendar.MONTH, 6);

        System.out.println("date = " + date);
        System.out.println("date1 = " + date1);
        System.out.println("date2 = " + date2);
    }

    public void testGetCountryIsoCodeAlpha2ByIP() throws Exception {
        String IP = "49.225.45.115";
        // String IP = "192.168.0.3";
        System.out.println("IP = " + IP);
        String isoCode = CommonUtil.getCountryIsoCodeAlpha2ByIP(IP);
        System.out.println("isoCode = " + isoCode);
    }

    public void testGetCountryIsoCodeAlpha2ByCountry() throws Exception {
        // List of supported countries and territories
        String[] countries = {
            "Bolivia, Plurinational State of"
            // Add the rest of the countries
        };

        for(String country: countries) {
            String isoCode = CommonUtil.getCountryIsoCodeAlpha2ByCountry(country);
            System.out.println(country + ": " + isoCode);
        } 
    }

    public void testGetAllSupportedCountries() throws Exception {
        String fileName = "WhiteListRegions.txt";

        List<String> countries = ConfigUtil.getTextFileInLines(fileName);
        for(String country: countries) {
            String isoCode = CommonUtil.getCountryIsoCodeAlpha2ByCountry(country);
            if(isoCode == null) {
                System.out.println("country = " + country + ", isoCode = " + isoCode);
            }
            else {
                System.out.println(isoCode);
            }
        }
    }

    public void testSplitIntoSentences() {
        String text = "Hello world! This is a test. How are you? Let's split this into sentences.";
        List<String> sentences = CommonUtil.splitIntoSentences(text);
        
        for (String sentence : sentences) {
            System.out.println(sentence);
        }
    }
}

