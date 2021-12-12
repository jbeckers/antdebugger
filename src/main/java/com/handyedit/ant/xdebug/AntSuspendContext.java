package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 * Date: Nov 6, 2009
 */
class AntSuspendContext extends XSuspendContext {

    private AntExecutionStack myExecutionStack;
    private XExecutionStack @NotNull [] myExecutionStacks = XExecutionStack.EMPTY_ARRAY;


    AntSuspendContext(final Project project,
                      final @NotNull AntDebugProcess debugProcess) {
        AntDebuggerProxy debuggerProxy = debugProcess.myDebuggerProxy;
        if (!debuggerProxy.isReady()) {
            return;
        }

        myExecutionStack = new AntExecutionStack(project, debuggerProxy);
        myExecutionStacks = new XExecutionStack[1];
        myExecutionStacks[0] = myExecutionStack;
    }


    @Override
    public XExecutionStack getActiveExecutionStack() {
        return myExecutionStack;
    }

    @Override
    public XExecutionStack @NotNull [] getExecutionStacks() {
        return myExecutionStacks;
    }

}
