package com.sleazyweasel.applescriptifier;

import java.util.List;

public interface AppleScriptTemplate {
    public static int SPACE = 49;
    public static String PLUS = "+";
    public static String MINUS = "-";
    public static int RIGHT_ARROW = 124;
    public static int LEFT_ARROW = 123;
    public static int UP_ARROW = 126;
    public static int DOWN_ARROW = 125;

    <T> T execute(CharSequence script, String... args);

    <T> T execute(Application application, String... scriptLines);

    <T> T execute(Application application, List<String> args, String... scriptLines);

    boolean isRunning(Application application);

    void startApplication(Application application);

    boolean applicationExists(Application application);

    void executeKeyStroke(Application application, String keyStroke);

    void executeKeyCode(Application application, int keyCode);
}
