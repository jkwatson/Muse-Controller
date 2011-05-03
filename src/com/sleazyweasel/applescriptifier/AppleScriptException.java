package com.sleazyweasel.applescriptifier;

import javax.script.ScriptException;

public class AppleScriptException extends RuntimeException {
    public AppleScriptException(Exception e) {
        super(e);
    }
}
