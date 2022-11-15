package dev.porama.gradingcore.core.scorer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ScoringService {
    public void a(){
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine nashorn = factory.getEngineByName("nashorn");

    }
}
