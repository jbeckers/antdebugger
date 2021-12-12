package com.handyedit.ant.breakpoint;

import com.handyedit.ant.listener.AntBuildListener;
import com.handyedit.ant.listener.TempBreakpointType;
import com.handyedit.ant.listener.cmd.DebuggerCommandFactory;
import com.handyedit.ant.util.FileUtil;
import com.handyedit.ant.util.NetUtil;
import com.handyedit.ant.util.XmlUtil;
import com.handyedit.ant.xdebug.vars.AntVar;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.XDebuggerUtilImpl;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Proxy that communicates with Ant build process listener running in the Ant process.
 * Sends and reads commands. Read commands passed to Ant debug listeners.
 *
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class AntDebuggerProxy {

    private final XDebuggerUtilImpl myDebuggerUtil = new XDebuggerUtilImpl();

    private final Project myProject;

    private BufferedReader myReader;
    private BufferedWriter myWriter;

    private XSourcePosition myCurrentPosition;
    private final List<XSourcePosition> myStack = new ArrayList<>();

    private final Set<AntDebugListener> myListeners = new HashSet<>();

    private final Map<String, String> myVars = new HashMap<>();

    private final int myPort;

    AntDebuggerProxy(final Project project,
                     final int port) {
        myProject = project;
        myPort = port;
    }

    public synchronized boolean isReady() {
        return myWriter != null && myReader != null;
    }

    public boolean connect(final ProgressIndicator indicator, final ProcessHandler handler, final int seconds) throws IOException {
        try (Socket s = connect(seconds, handler, indicator)){
            if (s == null) {
                return false;
            }
            synchronized (this) {
                myReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                myWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            }
        }

        new Thread(() -> {
            try {
                readAntResponse();
            } catch (final IOException e) {
                // todo:
            }
        }).start();

        return true;
    }

    private Socket connect(final int times, final ProcessHandler handler, final ProgressIndicator indicator) {
        for (int i = 0; i < times && !indicator.isCanceled(); i++) {
            try {
                return new Socket(NetUtil.getLocalHost(), myPort);
            } catch (final IOException e) {
                try {
                    if (handler.isProcessTerminated()) {
                        throw new RuntimeException("Ant process terminated");
                    }
                    Thread.sleep(1000);
                } catch (final InterruptedException e1) {
                }
            }
        }

        return null;
    }

    private void readAntResponse() throws IOException {
        String line;
        while ((line = myReader.readLine()) != null) {
            String[] args = line.split(",");
            String cmd = args[0];
            if (AntBuildListener.CMD_VAR.equals(cmd)) {
                processVar(args);
            } else if (AntBuildListener.CMD_TARGET_START.equals(cmd)) {
                processTargetStart(args);
            } else if (AntBuildListener.CMD_TARGET_END.equals(cmd)) {
                processTargetEnd(args);
            } else {
                notifyListener(args);
            }
        }
    }

    private void processVar(final String @NotNull ... args) {
        if (args.length != 2 && args.length != 3) {
            return;
        }

        String val = args.length == 3 ? args[2] : null;
        myVars.put(args[1], val);
    }

    private void processTargetStart(final String @NotNull ... args) {
        int line = Integer.parseInt(args[1]);
        XSourcePosition pos = createPosition(args[2], line - 1);
        if (pos != null) {
            myStack.add(pos);
        }
    }

    private void processTargetEnd(final String @NotNull ... args) {
        myStack.remove(myStack.size() - 1);
    }

    private void notifyListener(final String @NotNull ... args) {
        String cmd = args[0];
        for (final AntDebugListener l: myListeners) {

            if (AntBuildListener.CMD_BREAKPOINT_STOP.equals(cmd)) {
                int line = Integer.parseInt(args[1]);
                String file = args[2];
                XSourcePosition pos = createPosition(file, line);
                if (pos != null) {
                    myCurrentPosition = pos;
                    l.onBreakpoint(new BreakpointPosition(file, line));
                }
            }
            if (AntBuildListener.CMD_BUILD_FINISHED.equals(cmd)) {
                l.onFinish();
            }
        }
    }

    private @Nullable XSourcePosition createPosition(final String file, final int line) {
        VirtualFile virtualFile = FileUtil.findFile(file);
        return virtualFile != null ? myDebuggerUtil.createPosition(virtualFile, line) : null;
    }

    public void attach(final @NotNull Collection<? extends BreakpointPosition> breakpoints) throws IOException {
        for (final BreakpointPosition position: breakpoints) {
            addBreakpoint(position);
        }
        resume();
    }

    public void addBreakpoint(final @NotNull BreakpointPosition pos) throws IOException {
        command(DebuggerCommandFactory.CMD_SET_BREAKPOINT, Integer.toString(pos.getLine()), pos.getFile());
    }

    public void removeBreakpoint(final @NotNull BreakpointPosition pos) throws IOException {
        command(DebuggerCommandFactory.CMD_REMOVE_BREAKPOINT, Integer.toString(pos.getLine()), pos.getFile());
    }

    public void resume() throws IOException {
        command(DebuggerCommandFactory.CMD_RESUME_EXECUTION);
    }

    public void runTo(final @NotNull XSourcePosition pos) {
        try {
            command(DebuggerCommandFactory.CMD_RUN_TO_CURSOR, Integer.toString(pos.getLine()), pos.getFile().getPath());
            resume();
        } catch (final IOException e) {
            // todo:
        }
    }

    public void stepInto() {
        try {
            setTempBreakpoint(TempBreakpointType.INTO);
        } catch (final IOException e) {
            // todo:
        }
    }

    public void stepOver() {
        try {
            setTempBreakpoint(TempBreakpointType.OVER);
        } catch (final IOException e) {
            // todo:
        }
    }

    public void stepOut() {
        try {
            setTempBreakpoint(TempBreakpointType.OUT);
        } catch (final IOException e) {
            // todo:
        }
    }

    private void setTempBreakpoint(final @NotNull TempBreakpointType type) throws IOException {
        command(DebuggerCommandFactory.CMD_SET_TEMP_BREAKPOINT, Integer.toString(type.getValue()));
        resume();
    }

    public void finish() {

    }

    public void addAntDebugListener(final AntDebugListener listener) {
        myListeners.add(listener);
    }

    public void removeAntDebugListener(final AntDebugListener listener) {
        myListeners.remove(listener);
    }

    private void command(final String... args) throws IOException {
        myWriter.write(StringUtils.join(args, ","));
        myWriter.newLine();
        myWriter.flush();
    }

    public AntFrame[] getFrames() {
        if (myCurrentPosition == null) {
            return new AntFrame[] {};
        }

        List<AntFrame> result = myStack.stream().map(this::createFrame).collect(Collectors.toList());
        result.add(createFrame(myCurrentPosition));
        Collections.reverse(result);
        AntFrame[] arr = new AntFrame[result.size()];
        result.toArray(arr);
        return arr;
    }

    @Contract("_ -> new")
    private @NotNull AntFrame createFrame(final XSourcePosition pos) {
        XmlTag tag = getTag(pos);
        String name = tag != null ? tag.getName() : "";
        boolean target = "target".equals(name);
        if (tag != null && target) {
            String targetName = tag.getAttributeValue("name");
            if (targetName != null) {
                name += " '" + targetName + '\'';
            }
        }
        return new AntFrame(pos, target, name);
    }

    private XmlTag getTag(final XSourcePosition pos) {
        final XmlTag[] result = new XmlTag[1];

        ApplicationManager.getApplication().runReadAction(() -> {
            XmlFile xmlFile = (XmlFile) PsiManager.getInstance(myProject).findFile(pos.getFile());
            if (xmlFile != null) {
                result[0] = XmlUtil.getTag(xmlFile, pos.getLine());
            }
        });

        return result[0];
    }

    public String getVariableValue(final String name) {
        return myVars.get(name);
    }

    public XValueChildrenList getVars() {
        List<String> names = new ArrayList<>(myVars.keySet());
        Collections.sort(names);

        XValueChildrenList result = new XValueChildrenList();

        for (final String key: names) {
            result.add(new AntVar(key, myVars.get(key)));
        }

        return result;
    }

    public int getPort() {
        return myPort;
    }

    public Project getProject() {
        return myProject;
    }
}
