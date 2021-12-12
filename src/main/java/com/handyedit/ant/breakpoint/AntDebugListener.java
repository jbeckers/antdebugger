package com.handyedit.ant.breakpoint;

import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public interface AntDebugListener {

    void onBreakpoint(@NotNull BreakpointPosition pos);

    void onFinish();
}
