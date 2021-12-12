package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.handyedit.ant.breakpoint.AntFrame;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
public class AntExecutionStack extends XExecutionStack {

    private final List<AntStackFrame> myFrames = new ArrayList<>();
    private AntStackFrame myTopFrame = null;

    AntExecutionStack(final Project project,
                      final AntDebuggerProxy debuggerProxy) {
        super("");

        if (!debuggerProxy.isReady()) {
            return;
        }
        try {
            AntFrame[] frames = debuggerProxy.getFrames();
            for (int i = 0; i < frames.length; i++) {
                final AntFrame antFrame = frames[i];
                AntStackFrame frame = new AntStackFrame(project, debuggerProxy, antFrame);
                myFrames.add(frame);
                if (i == 0) {
                    myTopFrame = frame;
                }
            }
        }
        catch (final Exception e) {
            myFrames.clear();
            myTopFrame = null;
        }
    }

    @Override
    public XStackFrame getTopFrame() {
        return myTopFrame;
    }

    @Override
    public void computeStackFrames(final int firstFrameIndex, final XStackFrameContainer container) {
        if (firstFrameIndex <= myFrames.size()) {
            container.addStackFrames(myFrames.subList(firstFrameIndex, myFrames.size()), true);
        }
    }
}
