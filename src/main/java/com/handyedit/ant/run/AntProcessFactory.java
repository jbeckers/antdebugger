package com.handyedit.ant.run;

import com.handyedit.ant.listener.AntBuildListener;
import com.handyedit.ant.util.FileUtil;
import com.handyedit.ant.util.IdeaConfigUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts.DialogMessage;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * @author Alexei Orischenko
 *         Date: Nov 10, 2009
 */
public class AntProcessFactory {

    private static final String ANT_MAIN_CLASS = "org.apache.tools.ant.launch.Launcher";
    private static final String ANT_LAUNCHER_JAR = "ant-launcher.jar";

    private boolean myDebug;
    private int myDebugPort;

    public OSProcessHandler createProcess(final AntRunConfiguration config) throws ExecutionException {
        GeneralCommandLine commandLine = create(config);
        if (commandLine == null) {
            return null;
        }
        Process p = commandLine.createProcess();
        String cmdString = commandLine.getCommandLineString();
        OSProcessHandler handler = new OSProcessHandler(p, cmdString);
        ProcessTerminatedListener.attach(handler);
        return handler;
    }


    private GeneralCommandLine create(final AntRunConfiguration config) {
        GeneralCommandLine result = new GeneralCommandLine();

        if (config.getJdkName() == null) {
            Messages.showErrorDialog("Please select Java SDK for the project", "Ant Debugger");
            return null;
        }

        String exePath = config.getJavaExePath();
        if (exePath == null) {
            Messages.showErrorDialog("Please configure Java SDK for module:\r\n" +
                    "current SDK is invalid: missing java executable", "Ant Debugger");
            return null;
        }

        result.setExePath(exePath);

        if (checkNotExists(config.getBuildFolder(), "Build folder")) {
            return null;
        }

        result.setWorkDirectory(config.getBuildFolder());

        String antHome = config.getAntHome();
        if (checkNotExists(antHome, "Ant home")) {
            return null;
        }

        result.addParameter("-Xmx" + config.getMaxMemory() + 'm');

        result.addParameter("-classpath");
        result.addParameter(getClassPath(config));

        result.addParameter("-Dant.home=" + antHome);
        String antLib = FileUtil.getPath(antHome, "lib");
        result.addParameter("-Dant.library.dir=" + antLib);

        if (checkNotExists(antLib, "Ant lib")) {
            return null;
        }

        if (myDebug) {
            result.addParameter("-D" + AntBuildListener.DEBUG_PORT_PROPERTY + '=' + myDebugPort);
        }

        String vmParams = config.getVmParameters();
        if (vmParams != null) {
            result.addParameter(vmParams);
        }

        result.addParameter(ANT_MAIN_CLASS);

        result.addParameter("-listener");
        result.addParameter(AntBuildListener.class.getName());

        AntLoggingLevel level = config.getLoggingLevel();
        if (AntLoggingLevel.QUIET.equals(level)) {
          result.addParameter("-q");
        }
        if (AntLoggingLevel.VERBOSE.equals(level)) {
          result.addParameter("-v");
        }
        if (AntLoggingLevel.DEBUG.equals(level)) {
          result.addParameter("-d");
        }

        if (!config.isDefaultBuildFile()) {
            result.addParameter("-buildfile");
            result.addParameter(config.getBuildFile().getName());
        }

        String targetName = config.getTargetName();
        if (targetName != null) {
            result.addParameter(targetName);
        }

        return result;
    }

    private static String getClassPath(final AntRunConfiguration config) {
        StringBuffer result = new StringBuffer();
        String antLib = FileUtil.getPath(config.getAntHome(), "lib");
        String antLauncher = FileUtil.getPath(antLib, ANT_LAUNCHER_JAR);
        FileUtil.addClasspath(antLauncher, result);

        if (checkNotExists(antLauncher, "Ant launcher")) {
            return null;
        }

        String toolsJar = config.getJdkTools();
        if (toolsJar != null) {
            FileUtil.addClasspath(toolsJar, result);
        }

        VirtualFile vAntLib = FileUtil.findFile(antLib);
        for (final VirtualFile child: vAntLib.getChildren()) {
            if (!child.isDirectory() && "jar".equals(child.getExtension())) {
                FileUtil.addClasspath(child.getPath(), result);
            }
        }

        String pluginLib = IdeaConfigUtil.getPluginClassesFolder(AntBuildListener.class);
        FileUtil.addClasspath(pluginLib, result);

        for (final String lib: config.getAdditionalSdkClasses()) {
            FileUtil.addClasspath(lib, result);
        }

        for (final String lib: config.getAdditionalAntClasses()) {
            FileUtil.addClasspath(lib, result);
        }

        return result.toString();
    }

    public static AntProcessFactory getInstance() {
        return new AntProcessFactory();
    }

    public static AntProcessFactory getInstance(final int debugPort) {
        AntProcessFactory res = new AntProcessFactory();
        res.myDebug = true;
        res.myDebugPort = debugPort;
        return res;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static boolean checkNotExists(final String path, @DialogMessage final String name) {
        if (!new File(path).exists()) {
            Messages.showErrorDialog(name + " doesn't exist: " + path, "Ant Debugger");
            return true;
        }
        return false;
    }
}
