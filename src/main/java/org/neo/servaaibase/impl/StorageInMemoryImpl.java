package org.neo.servaaibase.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

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
    }

    private Map<Object, StorageInMemoryImpl.Bucket> buckets = new HashMap<Object, StorageInMemoryImpl.Bucket>(); // not thread safe, it could be changed to CurrencyHashMap in need
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
        return null;
    }

    @Override
    public void addCodeRecord(Object key, AIModel.CodeRecord codeRecord) {
    }

    @Override
    public void clearCodeRecords(Object key) {
    }
}
