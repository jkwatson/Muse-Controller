package com.sleazyweasel.applescriptifier;

import javax.script.*;
import java.util.Arrays;
import java.util.List;

public class ScriptEngineAppleScriptTemplate implements AppleScriptTemplate {


    private ScriptEngine getEngine() {
        return new ScriptEngineManager().getEngineByName("AppleScript");
    }

    private <T> T execute(ScriptEngine engine, CharSequence script) {
        try {
            return (T) engine.eval(script.toString(), engine.getContext());
        } catch (ScriptException e) {
            throw new AppleScriptException(e);
        }
    }

    public <T> T execute(CharSequence script, String... args) {
        ScriptEngine engine = getEngine();
        if (args.length > 0) {
            setArgs(Arrays.asList(args), engine);
            return (T) execute(engine, script);
        } else {
            return (T) execute(engine, script);
        }
    }

    public <T> T execute(Application applicationName, String... scriptLines) {
        return (T) execute(getEngine(), applicationName, scriptLines);
    }

    private <T> T execute(ScriptEngine engine, Application applicationName, String... scriptLines) {
        StringBuilder command = new StringBuilder("tell application \"").append(applicationName.getName()).append("\"\n");
        for (String scriptLine : scriptLines) {
            command.append(scriptLine).append("\n");
        }

        command.append("end tell\n");
        return (T) execute(engine, command);
    }

    private <T> T execute(ScriptEngine engine, Application applicationName, Bindings bindings, String... scriptLines) {
        StringBuilder command = new StringBuilder("tell application ").append(applicationName.getName()).append("\n");
        for (String scriptLine : scriptLines) {
            command.append(scriptLine).append("\n");
        }

        command.append("end tell\n");
        return (T) execute(engine, command);
    }

    public <T> T execute(Application application, List<String> args, String... scriptLines) {
        ScriptEngine engine = getEngine();
        Bindings bindings = setArgs(args, engine);
        return (T) execute(engine, application, bindings, scriptLines);
    }

    public boolean isRunning(Application airfoil) {
        String isAirfoilRunningScript = "tell application \"System Events\"\n" +
                " set runningState to count (every process whose name is \"" + airfoil.getName() + "\")\n" +
                "end tell\n";
        Long numberOfProcesses = execute(isAirfoilRunningScript);
        return numberOfProcesses != null && numberOfProcesses > 0;
    }

    public void startApplication(Application application) {
        String startScript = "tell application \"" + application.getName() + "\" to activate";
        execute(startScript);
    }

    public boolean applicationExists(Application application) {
        //this is a bad, bad hack. fix it.
        if (application.equals(Application.MUSECONTROLLER)) {
            return NativePianobarSupport.isPianoBarSupportEnabled();
        }

        String query = "try\n" +
                "  tell application \"Finder\"\n" +
                "    return application file id \"" + application.getIdentifier() + "\"\n" +
                "  end tell\n" +
                "on error err_msg number err_num\n" +
                "  return null\n" +
                "end try";
        Object result = execute(query);
        return result != null;
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

    private Bindings setArgs(List<String> args, ScriptEngine engine) {
        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("javax_script_function", "dorun");
        //todo support more than one arg!
        bindings.put(ScriptEngine.ARGV, args.get(0));
        return bindings;
    }

}
