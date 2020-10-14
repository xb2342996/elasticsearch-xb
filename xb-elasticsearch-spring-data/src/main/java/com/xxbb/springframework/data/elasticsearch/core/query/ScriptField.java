package com.xxbb.springframework.data.elasticsearch.core.query;

import org.elasticsearch.script.Script;

public class ScriptField {
    private final String fieldName;
    private final Script script;

    public ScriptField(String fieldName, Script script) {
        this.fieldName = fieldName;
        this.script = script;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Script getScript() {
        return script;
    }
}
