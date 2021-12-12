package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.handyedit.ant.breakpoint.AntFrame;
import com.intellij.icons.AllIcons.Debugger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 * Date: Nov 6, 2009
 */
class AntStackFrame extends XStackFrame {

    private final AntDebuggerProxy myDebuggerProxy;
    private final XSourcePosition mySourcePosition;

    private final String myName;

    @Override
    public Object getEqualityObject() {
        return 0;
    }

    @Override
    public void computeChildren(@NotNull final XCompositeNode node) {
        try {
            node.addChildren(myDebuggerProxy.getVars(), true);
        } catch (final Exception e) {
            super.computeChildren(node);
        }
    }

    AntStackFrame(final Project project,
                  final AntDebuggerProxy debuggerProxy,
                  final @NotNull AntFrame frame) {
        myDebuggerProxy = debuggerProxy;
        mySourcePosition = frame.getSourcePosition();

        myName = frame.getName();
    }

    @Override
    public XSourcePosition getSourcePosition() {
        return mySourcePosition;
    }

    @Override
    public void customizePresentation(@NotNull final ColoredTextContainer component) {
        final XSourcePosition position = getSourcePosition();
        if (position != null) {
            component.append(myName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.append(position.getFile().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.append(":", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.append(Integer.toString(position.getLine() + 1), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.setIcon(Debugger.Frame);
        } else {
            component.append("Stack frame not available", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    @Override
    public XDebuggerEvaluator getEvaluator() {
        return new AntDebuggerEvaluator(this);
    }

    AntDebuggerProxy getDebuggerProxy() {
        return myDebuggerProxy;
    }
}
