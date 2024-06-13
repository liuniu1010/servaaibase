package org.neo.servaaibase;

import java.util.Map;
import java.util.HashMap;

public class NeoAIException extends RuntimeException {
    public static int NEOAIEXCEPTION_OTHER = 0;
    public static int NEOAIEXCEPTION_JSONSYNTAXERROR = 1;
    public static int NEOAIEXCEPTION_IOEXCEPTIONWITHLLM = 2;
    public static int NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX = 3;
    public static int NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED = 4;
   
    private static Map<Integer, String> defaultMapping = new HashMap<Integer, String>();
    static {
        defaultMapping.put(NEOAIEXCEPTION_OTHER, "Other reason");
        defaultMapping.put(NEOAIEXCEPTION_JSONSYNTAXERROR, "Json syntax error");
        defaultMapping.put(NEOAIEXCEPTION_IOEXCEPTIONWITHLLM, "IO Exceptin with LLM");
        defaultMapping.put(NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX, "IO Exceptin with sand box");
        defaultMapping.put(NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED, "Exceed max iteration deep");
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

