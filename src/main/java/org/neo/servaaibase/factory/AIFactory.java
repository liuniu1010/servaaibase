package org.neo.servaaibase.factory;

import org.neo.servaframe.interfaces.DBConnectionIFC;

import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.impl.OpenAIImpl;
import org.neo.servaaibase.impl.GoogleAIImpl;
import org.neo.servaaibase.util.CommonUtil;

public class AIFactory {
    public static SuperAIIFC getSuperAIInstance(DBConnectionIFC dbConnection) {
        String aiInstance = CommonUtil.getConfigValue(dbConnection, "AIInstance");
        if(aiInstance.equals("OpenAIImpl")) {
            return OpenAIImpl.getInstance(dbConnection);
        }
        else if(aiInstance.equals("GoogleAIImpl")) {
            return GoogleAIImpl.getInstance(dbConnection);
        }
        return null;
    }
}
