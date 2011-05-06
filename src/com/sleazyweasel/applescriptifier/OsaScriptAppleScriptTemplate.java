package com.sleazyweasel.applescriptifier;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OsaScriptAppleScriptTemplate implements AppleScriptTemplate {

    private <T> T execute(String... commands) {
        List<String> osaCommands = new ArrayList<String>();
        osaCommands.add("osascript");
        osaCommands.add("-ss");
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            osaCommands.add("-e");
            osaCommands.add(command);
        }
        String[] strings = osaCommands.toArray(new String[osaCommands.size()]);
        System.out.println("command = " + Arrays.toString(strings));
        try {
            Process process = Runtime.getRuntime().exec(strings);
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line;
            StringBuilder results = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                results.append(line);
                results.append("\n");
            }
            if (results.lastIndexOf("\n") > -1) {
                results.deleteCharAt(results.lastIndexOf("\n"));
            }
            System.out.println("results = " + results);
            return (T) new OsaScriptOutputConverter().convert(results.toString());
        } catch (IOException e) {
            throw new AppleScriptException(e);
        }
    }

    public <T> T execute(Application applicationName, String... scriptLines) {
        List<String> commands = new ArrayList<String>();
        commands.add("tell application \"" + applicationName.getName() + "\"");
        for (int i = 0; i < scriptLines.length; i++) {
            String scriptLine = scriptLines[i];
            commands.add(scriptLine);
        }
        commands.add("end tell");
        return (T) execute(commands.toArray(new String[commands.size()]));
    }

    public boolean isRunning(Application application) {
        String[] isAirfoilRunningScript = new String[]{"tell application \"System Events\"",
                " set runningState to count (every process whose name is \"" + application.getName() + "\")",
                "end tell"};
        String numberOfProcesses = execute(isAirfoilRunningScript);
        System.out.println("numberOfProcesses = " + numberOfProcesses);
        return numberOfProcesses != null && Integer.valueOf(numberOfProcesses) > 0;
    }

    public void startApplication(Application application) {
        String startScript = "tell application \"" + application.getName() + "\" to activate";
        execute(startScript);
    }

    public boolean applicationExists(Application application) {
        String[] query = new String[]{"try",
                "  tell application \"Finder\"",
                "    return application file id \"" + application.getIdentifier() + "\"",
                "  end tell",
                "on error err_msg number err_num",
                "  return null",
                "end try"};
        String result = execute(query);
        return result != null && !"null".equals(result);
    }

    public void executeKeyStroke(Application application, String keyStroke) {
        execute(application, "activate", "tell application \"System Events\" to keystroke \"" + keyStroke + "\"");
    }

    public void executeKeyStrokeWithCommandKey(Application application, String keyStroke) {
        execute(application, "activate", "tell application \"System Events\" to keystroke \"" + keyStroke + "\" using command down");
    }

    public void executeKeyCode(Application application, int keyCode) {
        execute(application, "activate", "tell application \"System Events\" to key code " + keyCode);
    }

}
