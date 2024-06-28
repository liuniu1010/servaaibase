package org.neo.servaaibase;

import java.util.Map;
import java.util.HashMap;

public class NeoAIException extends RuntimeException {
    public static int NEOAIEXCEPTION_OTHER = 0;
    public static int NEOAIEXCEPTION_JSONSYNTAXERROR = 1;
    public static int NEOAIEXCEPTION_IOEXCEPTIONWITHLLM = 2;
    public static int NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX = 3;
    public static int NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED = 4;
    public static int NEOAIEXCEPTION_SESSION_INVALID = 5;
    public static int NEOAIEXCEPTION_LOGIN_FAIL = 6;
    public static int NEOAIEXCEPTION_NOCREDITS_LEFT = 7;
    public static int NEOAIEXCEPTION_IN_MAINTENANCE = 8;
    public static int NEOAIEXCEPTION_USERNAME_IN_BLACKLIST = 9;
    public static int NEOAIEXCEPTION_IP_IN_BLACKLIST = 10;
    public static int NEOAIEXCEPTION_REGION_NOTIN_WHITELIST = 11;
    public static int NEOAIEXCEPTION_REGION_IN_BLACKLIST = 12;
   
    private static Map<Integer, String> defaultMapping = new HashMap<Integer, String>();
    static {
        defaultMapping.put(NEOAIEXCEPTION_OTHER, "Other reason");
        defaultMapping.put(NEOAIEXCEPTION_JSONSYNTAXERROR, "Json syntax error");
        defaultMapping.put(NEOAIEXCEPTION_IOEXCEPTIONWITHLLM, "IO Exceptin with LLM");
        defaultMapping.put(NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX, "IO Exceptin with sand box");
        defaultMapping.put(NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED, "Exceed max iteration deep");
        defaultMapping.put(NEOAIEXCEPTION_SESSION_INVALID, "Session Invalid");
        defaultMapping.put(NEOAIEXCEPTION_LOGIN_FAIL, "Username and password not matched");
        defaultMapping.put(NEOAIEXCEPTION_NOCREDITS_LEFT, "No credits left");
        defaultMapping.put(NEOAIEXCEPTION_IN_MAINTENANCE, "System in maintenance");
        defaultMapping.put(NEOAIEXCEPTION_USERNAME_IN_BLACKLIST, "Username in black list");
        defaultMapping.put(NEOAIEXCEPTION_IP_IN_BLACKLIST, "IP is not in service range");
        defaultMapping.put(NEOAIEXCEPTION_REGION_NOTIN_WHITELIST, "Region is not in service range");
        defaultMapping.put(NEOAIEXCEPTION_REGION_IN_BLACKLIST, "Region is not in service range");
    }

    private int code;

    public int getCode() {
        return code;
    }

    public NeoAIException(String inputMessage) {
        super(inputMessage);
        code = NEOAIEXCEPTION_OTHER;
    }

    public NeoAIException(int inputCode, Throwable cause) {
        super(cause.getMessage(), cause); 
        code = inputCode;
    }

    public NeoAIException(int inputCode, String inputMessage) {
        super(inputMessage); 
        code = inputCode;
    }

    public NeoAIException(int inputCode, String inputMessage, Throwable cause) {
        super(inputMessage, cause); 
        code = inputCode;
    }

    public NeoAIException(String inputMessage, Throwable cause) {
        super(inputMessage, cause);
        if(cause instanceof com.google.gson.JsonSyntaxException) {
            code = NEOAIEXCEPTION_JSONSYNTAXERROR; 
        }
        else {
            code = NEOAIEXCEPTION_OTHER; 
        }
    }

    public NeoAIException(int inputCode) {
        super(defaultMapping.get(inputCode));
        code = inputCode;
    }

    public NeoAIException(Throwable cause) {
        super(cause.getMessage(), cause);
        if(cause instanceof com.google.gson.JsonSyntaxException) {
            code = NEOAIEXCEPTION_JSONSYNTAXERROR; 
        }
        else {
            code = NEOAIEXCEPTION_OTHER; 
        }
    }
}

