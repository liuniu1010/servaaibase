package org.neo.servaaibase.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.model.SQLStruct;
import org.neo.servaframe.model.VersionEntity;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.model.AIModel;

public class StorageInDBImpl implements StorageIFC {
    private DBConnectionIFC dbConnection;
    private StorageInDBImpl() {
    }

    private StorageInDBImpl(DBConnectionIFC inputDBConnection) {
        dbConnection = inputDBConnection;
    }

    public static StorageIFC getInstance(DBConnectionIFC inputDBConnection) {
        return new StorageInDBImpl(inputDBConnection);
    }

    @Override
    public List<AIModel.ChatRecord> getChatRecords(Object key) {
        try {
            return innerGetChatRecords(key);
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<AIModel.ChatRecord> innerGetChatRecords(Object key) throws Exception {
        String sql = "select * from " + AIModel.ChatRecord.ENTITYNAME;
        sql += " where " + AIModel.ChatRecord.SESSION + " = ?";
        sql += " order by " + AIModel.ChatRecord.CHATTIME;
        List<Object> params = new ArrayList<Object>();
        params.add(key.toString());

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        List<VersionEntity> versionEntityList = dbConnection.queryAsVersionEntity(AIModel.ChatRecord.ENTITYNAME, sqlStruct);
        List<AIModel.ChatRecord> chatRecords = new ArrayList<AIModel.ChatRecord>();
        for(VersionEntity versionEntity: versionEntityList) {
            chatRecords.add(new AIModel.ChatRecord(versionEntity));
        }
        return chatRecords;
    }

    @Override
    public void addChatRecord(Object key, AIModel.ChatRecord chatRecord) {
        try {
            innerAddChatRecord(key, chatRecord);
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void innerAddChatRecord(Object key, AIModel.ChatRecord chatRecord) throws Exception{
        chatRecord.setSession(key.toString());
        VersionEntity versionEntity = chatRecord.getVersionEntity();
        dbConnection.insert(versionEntity);
    }

    @Override
    public void clearChatRecords(Object key) {
        try {
            innerClearChatRecords(key);
        }
        catch(RuntimeException rex) {
            throw rex;
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void innerClearChatRecords(Object key) throws Exception {
        String sql = "delete from " + AIModel.ChatRecord.ENTITYNAME;
        sql += " where " + AIModel.ChatRecord.SESSION + " = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(key.toString());
        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct);
    }
}
