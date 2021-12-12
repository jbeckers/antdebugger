package com.handyedit.ant.listener;

import java.util.Arrays;

public enum TempBreakpointType {
    INTO(0), OVER(1), OUT(2);

    private final int myValue;

    TempBreakpointType(final int value) {
        myValue = value;
    }

    public int getValue() {
        return myValue;
    }

    public static TempBreakpointType get(final int value) {
        return Arrays.stream(values()).filter(type -> type.getValue() == value).findFirst().orElse(OVER);
    }
}
