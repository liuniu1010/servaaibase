package org.neo.servaaibase;

import java.util.Map;
import java.util.HashMap;

public class NeoAIException extends RuntimeException {
    public static int NEOAIEXCEPTION_OTHER = 0;
    public static int NEOAIEXCEPTION_JSONSYNTAXERROR = 1;
    public static int NEOAIEXCEPTION_IOEXCEPTIONWITHLLM = 2;
   
    private static Map<Integer, String> defaultMapping = new HashMap<Integer, String>();
    static {
        defaultMapping.put(NEOAIEXCEPTION_OTHER, "other reason");
        defaultMapping.put(NEOAIEXCEPTION_JSONSYNTAXERROR, "Json syntax error");
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

