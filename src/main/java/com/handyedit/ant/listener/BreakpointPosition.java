package com.handyedit.ant.listener;

import org.apache.tools.ant.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Alexei Orischenko
 *         Date: Dec 9, 2009
 */
public class BreakpointPosition {
    private int myLine;
    private final String myFile;

    public BreakpointPosition(final int line,
                              final String file) {
        myLine = line;
        myFile = file;
    }

    BreakpointPosition(final @NotNull Location loc) {
        this(loc.getLineNumber() -1, loc.getFileName());
    }

    public int getLine() {
        return myLine;
    }

    public void setLine(final int line) {
        myLine = line;
    }

    @Override
    public String toString() {
        return myFile + ':' + myLine;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BreakpointPosition that = (BreakpointPosition) obj;
        return myLine == that.myLine && Objects.equals(myFile, that.myFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myLine, myFile);
    }
}

