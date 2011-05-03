package com.sleazyweasel.applescriptifier;

public class AppleScriptTemplateFactory {
    private static final boolean FORCE_OSA = false;

    public AppleScriptTemplate getActiveTemplate() {
        if (FORCE_OSA) {
            return new OsaScriptAppleScriptTemplate();
        }
        try {
            return new ScriptEngineAppleScriptTemplate();
        }
        catch (Throwable e) {
            return new OsaScriptAppleScriptTemplate();
        }
    }
}
