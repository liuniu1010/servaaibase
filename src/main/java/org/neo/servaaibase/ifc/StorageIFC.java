package org.neo.servaaibase.ifc;

import java.util.List;
import org.neo.servaaibase.model.AIModel;

public interface StorageIFC {
    public List<AIModel.ChatRecord> getChatRecords(Object key);
    public void addChatRecord(Object key, AIModel.ChatRecord chatRecord);
    public void clearChatRecords(Object key);

    public List<AIModel.CodeRecord> getCodeRecords(Object key);
    public void addCodeRecord(Object key, AIModel.CodeRecord codeRecord);
    public void clearCodeRecords(Object key);
}
