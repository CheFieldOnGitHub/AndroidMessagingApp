package com.che.messagedemo;

public class InitializationVariable {

    public int initialised;
    private onValueChangeListener valueChangeListener;

    public int isInitialised() {
        return initialised;
    }

    public void setVariable(int value) {
        initialised = value;
        if (valueChangeListener != null) valueChangeListener.onChange(initialised);
    }

    public onValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(onValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    public static interface onValueChangeListener {
        void onChange(int value);
    }
}
