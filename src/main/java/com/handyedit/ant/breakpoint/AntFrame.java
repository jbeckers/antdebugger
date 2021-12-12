package com.handyedit.ant.breakpoint;

import com.intellij.xdebugger.XSourcePosition;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
public class AntFrame {

    private final XSourcePosition mySourcePosition;
    private final boolean myTarget;
    private final String myName;

    AntFrame(final XSourcePosition pos,
             final boolean target,
             final String name) {
        mySourcePosition = pos;
        myTarget = target;
        myName = name;
    }

    public XSourcePosition getSourcePosition() {
        return mySourcePosition;
    }

    public boolean isTarget() {
        return myTarget;
    }

    public String getName() {
        return myName;
    }
}
