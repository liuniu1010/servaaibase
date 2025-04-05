package org.neo.servaaibase.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.neo.servaframe.model.NeoConcurrentHashMap;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.model.AIModel;

public class StorageInMemoryImpl implements StorageIFC {
    private static class Bucket {
        private List<AIModel.ChatRecord> chatRecords = new ArrayList<AIModel.ChatRecord>();

        public List<AIModel.ChatRecord> getChatRecords() {
            return chatRecords;
        } 

        public void addChatRecord(AIModel.ChatRecord chatRecord) {
            chatRecords.add(chatRecord);
        }

        public void clearChatRecords() {
            chatRecords.clear();
        }

        private List<AIModel.CodeRecord> codeRecords = new ArrayList<AIModel.CodeRecord>();

        public List<AIModel.CodeRecord> getCodeRecords() {
            return codeRecords;
        } 

        public void addCodeRecord(AIModel.CodeRecord codeRecord) {
            codeRecords.add(codeRecord);
        }

        public void clearCodeRecords() {
            codeRecords.clear();
        }

        private Stack<AIModel.CodeFeedback> codeFeedbackStack = new Stack<AIModel.CodeFeedback>();

        public AIModel.CodeFeedback peekCodeFeedback() {
            if(codeFeedbackStack.isEmpty()) {
                return null;
            }
            return codeFeedbackStack.peek();
        }

        public void pushCodeFeedback(AIModel.CodeFeedback codeFeedback) {
            codeFeedbackStack.push(codeFeedback);
        }

        public AIModel.CodeFeedback popCodeFeedback() {
            if(codeFeedbackStack.isEmpty()) {
                return null;
            }
            return codeFeedbackStack.pop();
        }

        public void clearCodeFeedbacks() {
            codeFeedbackStack.clear();
        }
    }

    private Map<Object, StorageInMemoryImpl.Bucket> buckets = new ConcurrentHashMap<Object, StorageInMemoryImpl.Bucket>();
    private StorageInMemoryImpl.Bucket getBucket(Object key) {
        if(!buckets.containsKey(key)) {
            StorageInMemoryImpl.Bucket bucket = new StorageInMemoryImpl.Bucket();
            buckets.put(key, bucket);
        }

        return buckets.get(key);
    }

    private StorageInMemoryImpl() {
    }

    private static StorageIFC instance = null;
 
    public static StorageIFC getInstance() {
        if(instance == null) {
            instance = new StorageInMemoryImpl();
        }

        return instance;
    }

    @Override
    public List<AIModel.ChatRecord> getChatRecords(Object key) {
        return getBucket(key).getChatRecords();
    }

    @Override
    public void addChatRecord(Object key, AIModel.ChatRecord chatRecord) {
        getBucket(key).addChatRecord(chatRecord);
    }

    @Override
    public void clearChatRecords(Object key) {
        getBucket(key).clearChatRecords();
    }

    @Override
    public List<AIModel.CodeRecord> getCodeRecords(Object key) {
        return getBucket(key).getCodeRecords();
    }

    @Override
    public void addCodeRecord(Object key, AIModel.CodeRecord codeRecord) {
        getBucket(key).addCodeRecord(codeRecord);
    }

    @Override
    public void clearCodeRecords(Object key) {
        getBucket(key).clearCodeRecords();
    }

    private Map<Object, AIModel.CodeFeedback> codeFeedbackCache = new NeoConcurrentHashMap<Object, AIModel.CodeFeedback>();
    @Override
    public AIModel.CodeFeedback peekCodeFeedback(Object key) {
        return getBucket(key).peekCodeFeedback();
    }

    @Override
    public void pushCodeFeedback(Object key, AIModel.CodeFeedback codeFeedback) {
        getBucket(key).pushCodeFeedback(codeFeedback);
    }

    @Override
    public AIModel.CodeFeedback popCodeFeedback(Object key) {
        return getBucket(key).popCodeFeedback();
    }

    @Override
    public void clearCodeFeedbacks(Object key) {
        getBucket(key).clearCodeFeedbacks();
    }
}
