package com.python.ide;

public class PythonCallback {

    private final TerminalActivity activity;

    public PythonCallback(
            TerminalActivity activity
    ){
        this.activity = activity;
    }

    public void requestInput(String prompt){
        activity.requestPythonInput(prompt);
    }

    public void appendOutput(String text){
        activity.appendOutput(text);
    }
}