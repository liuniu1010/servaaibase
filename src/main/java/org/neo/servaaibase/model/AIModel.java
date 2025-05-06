package org.neo.servaaibase.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.neo.servaframe.model.VersionEntity;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.NeoAIException;

public class AIModel {
    public static class CodeRecord {
        public static final String ENTITYNAME = "coderecord";
        private VersionEntity versionEntity = null;

        public static final String SESSION = "session";
        public static final String CREATETIME = "createtime";
        public static final String REQUIREMENT = "requirement";
        public static final String CONTENT = "content";

        public CodeRecord(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public CodeRecord(String session) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(SESSION, session);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getSession() {
            return (String)versionEntity.getAttribute(SESSION);
        }

        public void setSession(String session) {
            versionEntity.setAttribute(SESSION, session);
        }

        public Date getCreateTime() {
            return (Date)versionEntity.getAttribute(CREATETIME);
        }

        public void setCreateTime(Date inputCreateTime) {
            versionEntity.setAttribute(CREATETIME, inputCreateTime);
        }

        public String getRequirement() {
            return (String)versionEntity.getAttribute(REQUIREMENT);
        }

        public void setRequirement(String inputRequirement) {
            versionEntity.setAttribute(REQUIREMENT, inputRequirement);
        }

        public String getContent() {
            return (String)versionEntity.getAttribute(CONTENT);
        }

        public void setContent(String inputContent) {
            versionEntity.setAttribute(CONTENT, inputContent);
        }
    }

    public static class Jobs {
        public static final String ENTITYNAME = "jobs";
        private VersionEntity versionEntity = null;

        public static final String JOBID = "jobid";
        public static final String JOBTYPE = "jobtype";
        public static final String JOBSTATUS = "jobstatus";
        public static final String JOBPARAM = "jobparam";
        public static final String JOBOUTCOME = "joboutcome";
        public static final String CREATETIME = "createtime";
        public static final String EXPIRETIME = "expiretime";

        public Jobs(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public Jobs(String jobId) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(JOBID, jobId);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getJobId() {
            return (String)versionEntity.getAttribute(JOBID);
        }

        public void setJobId(String inputJobId) {
            versionEntity.setAttribute(JOBID, inputJobId);
        }

        public String getJobType() {
            return (String)versionEntity.getAttribute(JOBTYPE);
        }

        public void setJobType(String inputJobType) {
            versionEntity.setAttribute(JOBTYPE, inputJobType);
        }

        public String getJobStatus() {
            return (String)versionEntity.getAttribute(JOBSTATUS);
        }

        public void setJobStatus(String inputJobStatus) {
            versionEntity.setAttribute(JOBSTATUS, inputJobStatus);
        }

        public String getJobParam() {
            return (String)versionEntity.getAttribute(JOBPARAM);
        }

        public void setJobParam(String inputJobParam) {
            versionEntity.setAttribute(JOBPARAM, inputJobParam);
        }

        public String getJobOutcome() {
            return (String)versionEntity.getAttribute(JOBOUTCOME);
        }

        public void setJobOutcome(String inputJobOutcome) {
            versionEntity.setAttribute(JOBOUTCOME, inputJobOutcome);
        }

        public Date getCreatetime() {
            return (Date)versionEntity.getAttribute(CREATETIME);
        }

        public void setCreatetime(Date inputCreatetime) {
            versionEntity.setAttribute(CREATETIME, inputCreatetime);
        }

        public Date getExpiretime() {
            return (Date)versionEntity.getAttribute(EXPIRETIME);
        }

        public void setExpiretime(Date inputExpiretime) {
            versionEntity.setAttribute(EXPIRETIME, inputExpiretime);
        }
    }

    public static class ChatRecord {
        public static final String ENTITYNAME = "chatrecord";
        private VersionEntity versionEntity = null;

        public static final String SESSION = "session";
        public static final String CHATTIME = "chattime";
        public static final String CONTENT = "content";
        public static final String ATTACHMENTGROUP = "attachmentgroup";
        public static final String ISREQUEST = "isrequest";

        public ChatRecord(VersionEntity inputVersionEntity) {
            versionEntity = inputVersionEntity;
        }

        public ChatRecord(String session) {
            versionEntity = new VersionEntity(ENTITYNAME);
            versionEntity.setAttribute(SESSION, session);
        }

        public VersionEntity getVersionEntity() {
            return versionEntity;
        }

        public String getSession() {
            return (String)versionEntity.getAttribute(SESSION);
        }

        public void setSession(String session) {
            versionEntity.setAttribute(SESSION, session);
        }

        public Date getChatTime() {
            return (Date)versionEntity.getAttribute(CHATTIME);
        }

        public void setChatTime(Date inputChatTime) {
            versionEntity.setAttribute(CHATTIME, inputChatTime);
        }

        public String getContent() {
            return (String)versionEntity.getAttribute(CONTENT);
        }

        public void setContent(String inputContent) {
            versionEntity.setAttribute(CONTENT, inputContent);
        }

        public String getAttachmentGroup() {
            return (String)versionEntity.getAttribute(ATTACHMENTGROUP);
        }

        public void setAttachmentGroup(String inputAttachmentGroup) {
            versionEntity.setAttribute(ATTACHMENTGROUP, inputAttachmentGroup);
        }

        public boolean getIsRequest() {
            return (boolean)versionEntity.getAttribute(ISREQUEST);
        }

        public void setIsRequest(boolean inputIsRequest) {
            versionEntity.setAttribute(ISREQUEST, inputIsRequest);
        }
    }

    public static class PromptStruct {
        private List<AIModel.ChatRecord> chatRecords = new ArrayList<AIModel.ChatRecord>();
        private String userInput;
        private String systemHint;
        private AIModel.AttachmentGroup attachmentGroup;
        private FunctionCallIFC functionCall;

        public List<AIModel.ChatRecord> getChatRecords() {
            return chatRecords;
        }

        public void setChatRecords(List<AIModel.ChatRecord> inputChatRecords) {
            chatRecords = inputChatRecords;
        }

        public String getUserInput() {
            return userInput;
        }

        public void setUserInput(String inputUserInput) {
            userInput = inputUserInput;
        }

        public String getSystemHint() {
            return systemHint;
        }

        public void setSystemHint(String inputSystemHint) {
            systemHint = inputSystemHint;
        }

        public AIModel.AttachmentGroup getAttachmentGroup() {
            return attachmentGroup;
        }

        public void setAttachmentGroup(AIModel.AttachmentGroup inputAttachmentGroup) {
            attachmentGroup = inputAttachmentGroup;
        }

        public FunctionCallIFC getFunctionCall() {
            return functionCall;
        }

        public void setFunctionCall(FunctionCallIFC inputFunctionCall) {
            functionCall = inputFunctionCall;
        }
    }

    public static class ImagePrompt {
        private String userInput;
        private String size = "1024x1024";   // default as 1024x1024
        private int number = 1; // default as 1

        public String getUserInput() {
            return userInput;
        }

        public void setUserInput(String inputUserInput) {
            userInput = inputUserInput;
        }
       
        public String getSize() {
            return size;
        }

        public void setSize(String inputSize) {
            size = inputSize;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int inputNumber) {
            number = inputNumber;
        }
    }

    public static class TextToSpeechPrompt {
        private String userInput;
        private String voice = "alloy";   // default as alloy
        private String outputFormat = "mp3";  // default as mp3

        public String getUserInput() {
            return userInput;
        }

        public void setUserInput(String inputUserInput) {
            userInput = inputUserInput;
        }

        public String getVoice() {
            return voice;
        }

        public void setVoice(String inputVoice) {
            voice = inputVoice;
        }

        public String getOutputFormat() {
            return outputFormat;
        }

        public void setOutputFormat(String inputOutputFormat) {
            outputFormat = inputOutputFormat;
        }
    }

    public static class TokensUsage {
        private int inputTokens;
        private int outputTokens;
        private int cachedTokens;

        public TokensUsage() {
            inputTokens = 0;
            outputTokens = 0;
            cachedTokens = 0;
        }

        public int getInputTokens() {
            return inputTokens;
        }

        public void setInputTokens(int inputInputTokens) {
            inputTokens = inputInputTokens;
        }

        public int getOutputTokens() {
            return outputTokens;
        }

        public void setOutputTokens(int inputOutputTokens) {
            outputTokens = inputOutputTokens;
        }

        public int getCachedTokens() {
            return cachedTokens;
        }

        public void setCachedTokens(int inputCachedTokens) {
            cachedTokens = inputCachedTokens;
        }

        @Override
        public String toString() {
            String str = "inputTokens = " + inputTokens;
            str += "\noutputTokens = " + outputTokens;
            str += "\ncachedTokens = " + cachedTokens;
            return str;
        }
    }

    public static class ChatResponse {
        private boolean isSuccess;
        private String message;   // in case isSuccess is false, message is exception info
        private List<AIModel.Call> calls;
        private TokensUsage tokensUsage;

        public ChatResponse(boolean inputIsSuccess, String inputMessage) {
            isSuccess = inputIsSuccess;
            message = inputMessage;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public List<AIModel.Call> getCalls() {
            return calls;
        }

        public void setCalls(List<AIModel.Call> inputCalls) {
            calls = inputCalls;
        }

        public TokensUsage getTokensUsage() {
            return tokensUsage;
        }

        public void setTokensUsage(TokensUsage inputTokensUsage) {
            tokensUsage = inputTokensUsage;
        }
    }

    public static class FunctionParam {
        private String name;    // basically, only string type are supported, so no need to define type
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String inputName) {
            name = inputName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String inputDescription) {
            description = inputDescription;
        }
    }

    public static class Function {
        private String methodName;
        private String description;
        private List<FunctionParam> params;
 
        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String inputMethodName) {
            methodName = inputMethodName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String inputDescription) {
            description = inputDescription;
        }

        public List<FunctionParam> getParams() {
            return params;
        }

        public void setParams(List<FunctionParam> inputParams) {
            params = inputParams;
        }
    }

    public static class CallParam {
        public static String UNKNOWN = "unknown";
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String inputName) {
            name = inputName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String inputValue) {
            value = inputValue;
        }

        @Override
        public String toString() {
            String str = "paramName = " + name;
            str += "\nparamValue = " + value;
            return str;
        }
    }

    public static class Call {
        private String methodName;
        private List<CallParam> params;
 
        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String inputMethodName) {
            methodName = inputMethodName;
        }

        public List<CallParam> getParams() {
            return params;
        }

        public void setParams(List<CallParam> inputParams) {
            params = inputParams;
        }

        @Override
        public String toString() {
            String str = "methodName = " + methodName;
            if(params != null) {
                for(CallParam param: params) {
                    str += "\n" + param.toString();
                }
            }
            return str;
        }
    }

    public static class Embedding {
        private double[] data;
        private TokensUsage tokensUsage;

        public Embedding(double[] inputData) {
            data = inputData;
        }

        public int size() {
            return (data == null)?0:data.length;
        }

        public double get(int index) {
            return data[index];
        }

        public TokensUsage getTokensUsage() {
            return tokensUsage;
        }

        public void setTokensUsage(TokensUsage inputTokensUsage) {
            tokensUsage = inputTokensUsage;
        }

        @Override
        public String toString() {
            String str = "[";
            for(int i = 0;i < this.size();i++) {
                if(i > 0) {
                    str += ", ";
                }
                str += data[i];
            }
            str += "]";

            return str;
        }
    }

    public static class AttachmentGroup {
        List<Attachment> attachments;

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> inputAttachments) {
            attachments = inputAttachments;
        }

        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "attachmentGroup");
 
            JsonArray jsonArray = new JsonArray();
            for(Attachment attachment: attachments) {
                JsonObject jsonAttachment = attachment.toJsonObject();
                jsonArray.add(jsonAttachment);
            }

            jsonObject.add("attachments", jsonArray);

            return jsonObject;
        }

        public static AttachmentGroup fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("attachmentGroup")) {
                throw new NeoAIException("this jsonObject is not an AttachmentGroup");
            }

            AttachmentGroup attachmentGroup = new AttachmentGroup();
            List<Attachment> attachments = new ArrayList<Attachment>();
            JsonArray jsonArray = jsonObject.getAsJsonArray("attachments");
            for(int i = 0;i < jsonArray.size();i++) {
                JsonObject jsonAttachment = jsonArray.get(i).getAsJsonObject();
                Attachment attachment = Attachment.fromJsonObject(jsonAttachment);
                attachments.add(attachment);
            }

            attachmentGroup.setAttachments(attachments);
            return attachmentGroup;
        }
    }

    public static class Attachment {
        private String content;  // the content could be url or base64 with mime type

        public String getContent() {
            return content;
        }

        public void setContent(String inputContent) {
            content = inputContent;
        }

        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "attachment");
            jsonObject.addProperty("content", content);

            return jsonObject;
        }

        public static Attachment fromJsonObject(JsonObject jsonObject) {
            if(!jsonObject.get("type").getAsString().equals("attachment")) {
                throw new NeoAIException("this jsonObject is not an attachment");
            }

            Attachment attachment = new Attachment();
            attachment.setContent(jsonObject.get("content").getAsString());
            return attachment;
        }
    }

    public static class CodeFeedback {
        private String session;
        private String codeContent;
        private String feedback;

        public final static int INDEX_CODECONTENT = 1;
        public final static int INDEX_FEEDBACK = 2;
        private int index;

        private CodeFeedback() {
        }

        public CodeFeedback(String inputSession) {
            session = inputSession;
            codeContent = "";
            feedback = "";
            index = INDEX_FEEDBACK;
        }

        public String getSession() {
            return session;
        }

        public void setSession(String inputSession) {
            session = inputSession;
        }

        public String getCodeContent() {
            return codeContent;
        }

        public void setCodeContent(String inputCodeContent) {
            codeContent = inputCodeContent;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String inputFeedback) {
            feedback = inputFeedback;
        }

        public void setIndex(int inputIndex) {
            index = inputIndex;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            if(index == INDEX_CODECONTENT) {
                return codeContent;
            }
            return feedback;
        }
    }
}
