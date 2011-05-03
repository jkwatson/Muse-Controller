package com.sleazyweasel.applescriptifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OsaScriptOutputConverter {
    
    public <T> T convert(String output) {
        Object results = descend(output, new AtomicInteger(0));
        return (T) results;
    }

    private <T> T descend(String output, AtomicInteger position) {
        Object mapOrList = null;
        boolean inQuotes = false;
        char[] characters = output.toCharArray();
        StringBuilder textBuilder = null;
        String latestText = null;
        String latestKey = null;
        Object latestSubObject = null;
        boolean insideBrackets = false;
        StringBuilder stuffInsideBrackets;
        for (int i = position.get(); position.get() < characters.length; i = position.incrementAndGet()) {
            char aByte = characters[i];
            if (aByte == '"' && !inQuotes) {
                inQuotes = true;
                textBuilder = new StringBuilder();
            }
            else if (aByte == '"' && inQuotes) {
                inQuotes = false;
                latestText = textBuilder.toString();
            }
            else if (inQuotes) {
                textBuilder.append((char) aByte);
            }
            else if (aByte == '{') {
                position.incrementAndGet();
                latestSubObject = descend(output, position);
            }
            else if (aByte == '}') {
                if (mapOrList instanceof Map) {
                    latestKey = latestKey.replaceAll("\\|", "");
                    ((Map) mapOrList).put(latestKey, textBuilder.toString());
                }
                else {
                    if (mapOrList == null) {
                        mapOrList = new ArrayList();
                    }
                    if (latestSubObject != null ) {
                        ((List) mapOrList).add(latestSubObject);
                        latestSubObject = null;
                    } else {
                        if (textBuilder != null) {
                            ((List) mapOrList).add(textBuilder.toString());
                        }
                    }
                }
                return (T) mapOrList;
            }
            else if (aByte == ':') {
                if (mapOrList == null) {
                    mapOrList = new HashMap();
                }
                latestKey = textBuilder.toString();
                textBuilder = new StringBuilder();
            }
            else if (aByte == ' ') {
                //skip spaces if not in a quoted area.
            }
            else if (aByte == ',') {
                if (mapOrList instanceof Map) {
                    latestKey = latestKey.replaceAll("\\|", "");
                    ((Map) mapOrList).put(latestKey, textBuilder.toString());
                }
                else {
                    if (mapOrList == null) {
                        mapOrList = new ArrayList();
                    }
                    if (latestSubObject != null ) {
                        ((List) mapOrList).add(latestSubObject);
                        latestSubObject = null;
                    }
                    else {
                        ((List) mapOrList).add(textBuilder.toString());
                    }
                }
                textBuilder = new StringBuilder();
            }
            else {
                if (textBuilder == null) {
                    textBuilder = new StringBuilder();
                }
                textBuilder.append((char) aByte);
            }
        }
        if (textBuilder != null && textBuilder.length() > 0) {
            latestText = textBuilder.toString();
        }

        return (T) (latestSubObject != null ? latestSubObject : latestText);
    }
}
