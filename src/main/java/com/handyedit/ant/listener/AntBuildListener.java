package com.handyedit.ant.listener;

import com.handyedit.ant.util.StringUtil;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.Path;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Ant build listener that notifies IDE about reaching a task or target start (by build process)
 * and suspends build on task or target breakpoint (waits resume command from IDE).
 *
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntBuildListener implements BuildListener {

    // commands sent to IDE
    public static final String CMD_VAR = "var";
    public static final String CMD_TARGET_START = "target-start";
    public static final String CMD_TARGET_END = "target-end";
    public static final String CMD_BREAKPOINT_STOP = "stop";
    public static final String CMD_BUILD_FINISHED = "finish";

    private static final Set<String> IGNORED_TASKS = createIgnored();

    public static final String DEBUG_PORT_PROPERTY = "jetbrains.ant.debug.port";

    private BreakpointManager myManager;

    private @Nullable DebuggerCommandListener myListener;

    @Override
    public void buildStarted(final BuildEvent buildEvent) {
        myManager = new BreakpointManager();

        try {
            String portStr = System.getProperty(DEBUG_PORT_PROPERTY);
            if (portStr != null) {
                myListener = DebuggerCommandListener.start(myManager, Integer.parseInt(portStr));
            }
        } catch (final IOException e) {
            onError();
        }
    }

    @Override
    public void buildFinished(final BuildEvent buildEvent) {
        try {
            if (myListener != null) {
                myListener.sendCommand(CMD_BUILD_FINISHED);
            }
        } catch (final IOException e) {
            onError();
        }
    }

    @Override
    public void targetStarted(final BuildEvent buildEvent) {
        try {
            Target target = buildEvent.getTarget();
            Location location = target.getLocation();
            if (myListener != null) {
                BreakpointPosition pos = new BreakpointPosition(location);
                myManager.setCurrentPosition(pos);
                myManager.onTargetStart(pos);
                String line = Integer.toString(location.getLineNumber());
                myListener.sendCommand(CMD_TARGET_START, line, location.getFileName());
                onBreakpoint(location, buildEvent);
            }
        } catch (final Exception e) {
            onError();
        }
    }

    private void onBreakpoint(final Location location, final BuildEvent event) throws InterruptedException {
        if (Location.UNKNOWN_LOCATION.equals(location)) {
            return;
        }

        int line = location.getLineNumber() - 1;
        boolean tempBreakpoint = isTempBreakpoint(event);
        BreakpointPosition pos = new BreakpointPosition(location);
        boolean runToBreakpoint = myManager.isRunToBreakpoint(pos);

        if ((tempBreakpoint || runToBreakpoint || myManager.isBreakpoint()) && myListener != null) {
            try {
                if (tempBreakpoint) {
                    myManager.removeTemp();
                }
                if (runToBreakpoint) {
                    myManager.removeRunTo(pos);
                }

                sendVars(event);
                sendRefs(event);
                myListener.sendCommand(CMD_BREAKPOINT_STOP, Integer.toString(line), location.getFileName());
                myManager.waitResume();
            } catch (final Exception e) {
                onError();
            }
        }
    }

    private boolean isTempBreakpoint(final BuildEvent e) {
        return myManager.isTempBreakpoint() && !isIgnored(e.getTask(), e.getTarget());
    }

    @Override
    public void targetFinished(final BuildEvent buildEvent) {
        try {
            if (myListener != null) {
                myManager.onTargetEnd();

                myListener.sendCommand(CMD_TARGET_END);
            }
        } catch (final IOException e) {
            onError();
        }
    }

    @Override
    public void taskStarted(final BuildEvent buildEvent) {
        try {
            if (myListener != null) {
                Location taskLocation = buildEvent.getTask().getLocation();
                Location targetLocation = buildEvent.getTarget().getLocation();
                myManager.setCurrentPosition(new BreakpointPosition(taskLocation), new BreakpointPosition(targetLocation));
                onBreakpoint(taskLocation, buildEvent);
            }
        } catch (final InterruptedException e) {
            onError();
        }
    }

    @Override
    public void taskFinished(final BuildEvent buildEvent) {
    }

    @Override
    public void messageLogged(final BuildEvent buildEvent) {
    }

    private void onError() {
        if (myListener != null) {
            myListener.close();
            myListener = null;
        }
    }

    private void sendVars(final BuildEvent e) throws IOException {
        Map<String, Object> props = e.getProject().getProperties();
        if (props != null && myListener != null) {
            for (final Entry<String, Object> entry: props.entrySet()) {
                if (entry.getValue() != null) {
                    myListener.sendCommand(CMD_VAR, entry.getKey(), entry.getValue().toString());
                } else {
                    myListener.sendCommand(CMD_VAR, entry.getKey());
                }
            }
        }
    }

    private void sendRefs(final BuildEvent e) throws IOException {
        Map<String, Object> refs = e.getProject().getReferences();
        if (refs != null && myListener != null) {
            for (final Entry<String, Object> entry: refs.entrySet()) {
                if (entry.getValue() != null && entry.getValue() instanceof Path) {
                    myListener.sendCommand(CMD_VAR, entry.getKey(), StringUtil.removeLineFeeds(entry.getValue().toString()));
                }
            }
        }
    }

    static void log(final String msg) {
        try {
            File file = new File("/projects/debug.log");
            BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
            out.write(msg);
            out.newLine();
            out.close();
        } catch (final IOException ignored) {

        }
    }

    private static Set<String> createIgnored() {
        Set<String> result = new HashSet<>();
        result.add("import");
        result.add("property");
        result.add("xmlproperty");
        result.add("loadproperties");
        result.add("taskdef");
        result.add("typedef");
        result.add("patternset");
        result.add("path");
        result.add("tstamp");
        return result;
    }

    private boolean isIgnored(final Task task, final Target parentTarget) {
        return task == null
                || parentTarget == null
                || task.getTaskName() == null
                || IGNORED_TASKS.contains(task.getTaskName()) && !myManager.isCurrentTarget(parentTarget.getLocation());

    }
}
