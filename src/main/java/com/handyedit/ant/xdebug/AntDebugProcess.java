package com.handyedit.ant.xdebug;

import com.handyedit.ant.breakpoint.AntDebugListener;
import com.handyedit.ant.breakpoint.AntDebuggerProxy;
import com.handyedit.ant.breakpoint.BreakpointPosition;
import com.handyedit.ant.util.ConsoleUtil;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntDebugProcess extends XDebugProcess {

    private boolean isSuspended = false;

    private final XBreakpointHandler[] myBreakPointHandlers;
    AntDebuggerProxy myDebuggerProxy;
    private final AntDebugListener myDebugListener;
    private final RunProfileState myState;
    private final AntLineBreakpointHandler myLineBreakpointHandler;


    private final ProcessHandler myOSProcessHandler;


    @Override
    public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
        return new AntDebuggerEditorsProvider();
    }

    public AntDebugProcess(@NotNull final XDebugSession session,
                           @NotNull final RunProfileState state,
                           @Nullable final ProcessHandler processHandler,
                           @NotNull final AntDebuggerProxy debuggerProxy) {
        super(session);
        myState = state;
        myDebuggerProxy = debuggerProxy;
        myOSProcessHandler = processHandler;
        myDebugListener = new MyAntDebugListener(session.getProject());

        myLineBreakpointHandler = new AntLineBreakpointHandler(this);
        myBreakPointHandlers = new XBreakpointHandler[]{ myLineBreakpointHandler };

        myDebuggerProxy.addAntDebugListener(myDebugListener);
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();

        ProgressManager.getInstance().run(new Backgroundable(null, "Ant debugger", true) {
            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                indicator.setText("Connecting...");
                indicator.setIndeterminate(true);

                try {
                    final boolean connectFailed = !myDebuggerProxy.connect(indicator, myOSProcessHandler, 60);

                    if (connectFailed) {
                        terminateDebug(null);
                        return;
                    }

                    if (myDebuggerProxy.isReady()) {
                        myDebuggerProxy.attach(myLineBreakpointHandler.myBreakpointByPosition.keySet());
                    } else {
                        terminateDebug(null);
                    }

                } catch (final Exception e) {
                    terminateDebug(e.getMessage());
                }
            }

            private void terminateDebug(final String msg) {
                getProcessHandler().destroyProcess();
                invokeLater(() -> {
                    String text = "Debugger can't connect to Ant on port " + myDebuggerProxy.getPort();
                    Messages.showErrorDialog(msg != null ? text + ":\r\n" + msg : text, "Ant Debugger");
                });
            }
        });
    }

    @Override
    @Nullable
    protected ProcessHandler doGetProcessHandler() {
        return myOSProcessHandler;
    }

    @NotNull
    @Override
    public ExecutionConsole createConsole() {
        return ConsoleUtil.createAttachedConsole(getSession().getProject(), getProcessHandler());
    }

    @Override
    public void startStepInto(@Nullable final XSuspendContext context) {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.stepInto();
        }
    }

    @Override
    public void startStepOver(@Nullable final XSuspendContext context) {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.stepOver();
        }
    }

    @Override
    public void startStepOut(@Nullable final XSuspendContext context) {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.stepOut();
        }
    }

    @Override
    public void runToPosition(@NotNull final XSourcePosition position,
                              @Nullable final XSuspendContext context) {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.runTo(position);
        }
    }

    @Override
    public void stop() {
        if (myDebuggerProxy.isReady()) {
            myDebuggerProxy.removeAntDebugListener(myDebugListener);
            if (isSuspended) {
                getSession().resume();
            }
            myDebuggerProxy.finish();
        }
    }

    @Override
    public void resume(@Nullable final XSuspendContext context) {
        isSuspended = false;
        if (myDebuggerProxy.isReady()) {
            try {
                myDebuggerProxy.resume();
            } catch (final IOException e) {
                // todo:
            }
        }
    }

    @Override
    public XBreakpointHandler<?> @NotNull [] getBreakpointHandlers() {
        return myBreakPointHandlers;
    }

    void removeBreakPoint(final BreakpointPosition breakpoint) {
        if (myDebuggerProxy.isReady()) {
            try {
                myDebuggerProxy.removeBreakpoint(breakpoint);
            } catch (final IOException e) {
                // todo
            }
        }
    }

    void addBreakPoint(final BreakpointPosition breakpoint) {
        if (myDebuggerProxy.isReady()) {
            try {
                myDebuggerProxy.addBreakpoint(breakpoint);
            } catch (final IOException e) {
                // todo:
            }
        }
    }

    Project getProject() {
        return myDebuggerProxy.getProject();
    }

    private final class MyAntDebugListener implements AntDebugListener {

        private final Project myProject;

        private MyAntDebugListener(final Project project) {
            myProject = project;
        }

        @Override
        public void onBreakpoint(final @NotNull BreakpointPosition pos) {
            final XDebugSession debugSession = getSession();


            isSuspended = true;
            final XBreakpoint xBreakpoint = myLineBreakpointHandler.myBreakpointByPosition.get(pos);

            AntSuspendContext suspendContext = new AntSuspendContext(myProject, AntDebugProcess.this);

            if (xBreakpoint != null) {
                if (debugSession.breakpointReached(xBreakpoint, xBreakpoint.getLogExpressionObject().getExpression(), suspendContext)) {
                } else {
                    resume(suspendContext);
                }
            } else {
                debugSession.positionReached(suspendContext);
            }
        }

        @Override
        public void onFinish() {
            getSession().stop();
        }
    }
}
