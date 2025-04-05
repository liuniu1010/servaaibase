package org.neo.servaaibase.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.model.SQLStruct;
import org.neo.servaframe.model.VersionEntity;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

public class StorageInDBImpl implements StorageIFC {
    private static int MAX_TEXT_BYTE_LENGTH = 65535;
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
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
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
            chatRecord.setContent(CommonUtil.truncateTextFromStart(chatRecord.getContent(), MAX_TEXT_BYTE_LENGTH));
            innerAddChatRecord(key, chatRecord);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
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
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
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

    @Override
    public List<AIModel.CodeRecord> getCodeRecords(Object key) {
        try {
            return innerGetCodeRecords(key);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    private List<AIModel.CodeRecord> innerGetCodeRecords(Object key) throws Exception {
        String sql = "select * from " + AIModel.CodeRecord.ENTITYNAME;
        sql += " where " + AIModel.CodeRecord.SESSION + " = ?";
        sql += " order by " + AIModel.CodeRecord.CREATETIME;
        List<Object> params = new ArrayList<Object>();
        params.add(key.toString());

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        List<VersionEntity> versionEntityList = dbConnection.queryAsVersionEntity(AIModel.CodeRecord.ENTITYNAME, sqlStruct);
        List<AIModel.CodeRecord> codeRecords = new ArrayList<AIModel.CodeRecord>();
        for(VersionEntity versionEntity: versionEntityList) {
            codeRecords.add(new AIModel.CodeRecord(versionEntity));
        }
        return codeRecords;
    }

    @Override
    public void addCodeRecord(Object key, AIModel.CodeRecord codeRecord) {
        try {
            codeRecord.setContent(CommonUtil.truncateTextFromStart(codeRecord.getContent(), MAX_TEXT_BYTE_LENGTH));
            innerAddCodeRecord(key, codeRecord);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    private void innerAddCodeRecord(Object key, AIModel.CodeRecord codeRecord) throws Exception{
        codeRecord.setSession(key.toString());
        VersionEntity versionEntity = codeRecord.getVersionEntity();
        dbConnection.insert(versionEntity);
    }

    @Override
    public void clearCodeRecords(Object key) {
        try {
            innerClearCodeRecords(key);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    private void innerClearCodeRecords(Object key) throws Exception {
        String sql = "delete from " + AIModel.CodeRecord.ENTITYNAME;
        sql += " where " + AIModel.CodeRecord.SESSION + " = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(key.toString());
        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct);
    }

    @Override
    public AIModel.CodeFeedback peekCodeFeedback(Object key) {
        throw new NeoAIException("not support");
    }

    @Override
    public void pushCodeFeedback(Object key, AIModel.CodeFeedback codeFeedback) {
        throw new NeoAIException("not support");
    }

    @Override
    public AIModel.CodeFeedback popCodeFeedback(Object key) {
        throw new NeoAIException("not support");
    }

    @Override
    public void clearCodeFeedbacks(Object key) {
        throw new NeoAIException("not support");
    }
}
