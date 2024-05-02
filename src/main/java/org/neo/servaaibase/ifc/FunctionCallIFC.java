package org.neo.servaaibase.ifc;

import java.util.List;
import org.neo.servaaibase.model.AIModel;

public interface FunctionCallIFC {
    public List<AIModel.Function> getFunctions();
    public Object callFunction(AIModel.Call call);
}
