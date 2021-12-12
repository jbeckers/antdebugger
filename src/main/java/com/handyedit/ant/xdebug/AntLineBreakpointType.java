package com.handyedit.ant.xdebug;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntLineBreakpointType extends XLineBreakpointType<XBreakpointProperties> {

    private final XDebuggerEditorsProvider myEditorsProvider = new AntDebuggerEditorsProvider();

    public AntLineBreakpointType() {
        super("ant-line", "Ant breakpoints");
    }

    @Override
    public XBreakpointProperties createBreakpointProperties(@NotNull final VirtualFile file, final int line) {
        return null;
    }

    @Override
    public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
        return "xml".equals(file.getExtension());
    }

    @Override
    public @Nullable XDebuggerEditorsProvider getEditorsProvider(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint,
                                                                 @NotNull final Project project) {
        return myEditorsProvider;
    }
}
