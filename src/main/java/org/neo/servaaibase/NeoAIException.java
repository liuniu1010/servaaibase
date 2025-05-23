package org.neo.servaaibase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public static int NEOAIEXCEPTION_MAXREGISTERNUMBER_EXCEED = 13;
    public static int NEOAIEXCEPTION_MAXONLINENUMBER_EXCEED = 14;
    public static int NEOAIEXCEPTION_ADMIN_NOTIN_WHITELIST = 15;
    public static int NEOAIEXCEPTION_FILESIZE_EXCEED_UPPERLIMIT = 16;
    public static int NEOAIEXCEPTION_LLM_TOO_BUSY = 17;
    public static int NEOAIEXCEPTION_NOT_WORKING_THREAD = 18;
    public static int NEOAIEXCEPTION_JOB_NOTFOUND = 19;
   
    private static Map<Integer, String> defaultMapping = new ConcurrentHashMap<Integer, String>();
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
        defaultMapping.put(NEOAIEXCEPTION_MAXREGISTERNUMBER_EXCEED, "Exceed max register number");
        defaultMapping.put(NEOAIEXCEPTION_MAXONLINENUMBER_EXCEED, "Exceed max online number, please wait some moments and login again");
        defaultMapping.put(NEOAIEXCEPTION_ADMIN_NOTIN_WHITELIST, "This account is not administrator");
        defaultMapping.put(NEOAIEXCEPTION_FILESIZE_EXCEED_UPPERLIMIT, "File size exceed upper limit");
        defaultMapping.put(NEOAIEXCEPTION_LLM_TOO_BUSY, "Background LLM too busy");
        defaultMapping.put(NEOAIEXCEPTION_NOT_WORKING_THREAD, "Not working thread");
        defaultMapping.put(NEOAIEXCEPTION_JOB_NOTFOUND, "Job not found");
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

