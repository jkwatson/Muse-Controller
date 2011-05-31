package com.sleazyweasel.applescriptifier;

import java.util.LinkedList;

public class LineBuffer {
    private final int size;

    private final LinkedList<Character> data;

    public LineBuffer(int size) {
        this.size = size;
        data = new LinkedList<Character>();
    }

    public synchronized void add(Character character) {
        if (data.size() == size) {
            data.remove();
        }
        data.add(character);
    }

    public synchronized String getContents() {
        StringBuilder stringBuilder = new StringBuilder(data.size());
        for (Character character : data) {
            stringBuilder.append(character);
        }
        return stringBuilder.toString();
    }

    public synchronized boolean lastCharacterWasNewLine() {
        if (data.isEmpty()) {
            return false;
        }
        Character lastCharacter = data.get(data.size() - 1);
        return lastCharacter == 13 || lastCharacter == 10;
    }
}
