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

    public void testCommand() throws Exception {
        // String command = "/bin/sh -c \"ls -l | grep liuniu\"";
        String command = "ls -l /tmp/ | grep mysql";
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
}

