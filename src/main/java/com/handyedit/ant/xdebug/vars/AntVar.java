package com.handyedit.ant.xdebug.vars;

import com.handyedit.ant.util.StringUtil;
import com.intellij.icons.AllIcons.Debugger;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import org.jetbrains.annotations.NotNull;

/**
 * Variable renderer for the variables section (execution stack on the debugger tool window).
 *
 * @author Alexei Orischenko
 * Date: Nov 11, 2009
 */
public class AntVar extends XNamedValue {

    private final String myName;
    private final String myValue;

    public AntVar(final String name,
                  final String value) {
        super(name);
        myName = name;
        myValue = value;
    }

    @Override
    public void computePresentation(@NotNull final XValueNode node,
                                    @NotNull final XValuePlace place) {
        //Is this the same as import com.intellij.xdebugger.ui.DebuggerIcons.VALUE_ICON?
        node.setPresentation(
                Debugger.Value,
                new XStringValuePresentation(myValue != null ? StringUtil.quote(myValue) : "null"),
                false);
    }
}
