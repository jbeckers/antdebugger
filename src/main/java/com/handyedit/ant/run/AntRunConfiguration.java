package com.handyedit.ant.run;

import com.handyedit.ant.util.FileUtil;
import com.handyedit.ant.util.XmlUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexei Orischenko
 * Date: Nov 4, 2009
 */
public class AntRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule, Element> { // todo: store config between runs

    private static final String DEFAULT_BUILD_FILE = "build.xml";
    private static final int DEFAULT_DEBUG_PORT = 25000;
    private static final int DEFAULT_MAX_MEMORY = 256;

    private VirtualFile myBuildFile;
    private VirtualFile myTasksFolder;
    private int myDebugPort = DEFAULT_DEBUG_PORT;
    private String myJdkName;
    private String myTargetName;
    private String myVmParameters;

    private AntLoggingLevel myLoggingLevel = AntLoggingLevel.DEFAULT;

    private int myMaxMemory = DEFAULT_MAX_MEMORY;

    AntRunConfiguration(final Project project,
                        final ConfigurationFactory factory,
                        final String name) {
        super(name, new RunConfigurationModule(project), factory);
    }

    @Override
    public RunProfileState getState(@NotNull final Executor executor,
                                    @NotNull final ExecutionEnvironment environment) throws ExecutionException {
        return new AntRunCommandLineState(environment, this);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new AntRunSettingsEditor(
                ProjectJdkTable.getInstance().getSdksOfType(JavaSdk.getInstance()),
                getConfigurationModule().getProject());
    }

    @Override
    public Collection<Module> getValidModules() {
        return Collections.singleton(ModuleManager.getInstance(getProject()).getModules()[0]);
    }

    String getBuildFolder() {
        if (myBuildFile == null) {
            return null;
        }

        VirtualFile parent = myBuildFile.getParent();
        return parent != null
                ? parent.getPath()
                : null;
    }

    public VirtualFile getBuildFile() {
        return myBuildFile;
    }

    void setBuildFile(final VirtualFile file) {
        myBuildFile = file;
    }

    VirtualFile getTasksFolder() {
        return myTasksFolder;
    }

    void setTasksFolder(final VirtualFile folder) {
        myTasksFolder = folder;
    }

    boolean isDefaultBuildFile() {
        return myBuildFile != null && DEFAULT_BUILD_FILE.equals(myBuildFile.getName());
    }

    public int getDebugPort() {
        return myDebugPort;
    }

    void setDebugPort(final int debugPort) {
        myDebugPort = debugPort;
    }

    String getJavaExePath() {
        Sdk sdk = getSdk();
        JavaSdkType javaSdk = getJavaSdk(sdk);
        return sdk == null || javaSdk == null
                ? null
                : javaSdk.getVMExecutablePath(sdk);
    }


    String getJdkTools() {
        Sdk sdk = getSdk();
        JavaSdkType javaSdk = getJavaSdk(sdk);
        return sdk == null || javaSdk == null
                ? null
                : javaSdk.getToolsPath(sdk);
    }

    List<String> getAdditionalSdkClasses() {
        List<String> result = new ArrayList<>();

        Sdk sdk = getSdk();
        if (sdk != null) {
            String jrePath = sdk.getHomePath() + File.separator + "jre";

            result = Arrays.stream(sdk.getRootProvider().getUrls(OrderRootType.CLASSES)).map(PathUtil::toPresentableUrl)
                    .filter(url -> !url.startsWith(jrePath)).collect(Collectors.toList());
        }
        return result;
    }

    List<String> getAdditionalAntClasses() {
        List<String> result = new ArrayList<>();

        if (myTasksFolder != null) {
            result = Arrays.stream(myTasksFolder.getChildren())
                    .filter(child -> !child.isDirectory() && "jar".equals(child.getExtension()))
                    .map(VirtualFile::getPath).collect(Collectors.toList());
        }
        return result;
    }

    private static JavaSdkType getJavaSdk(final Sdk sdk) {
        if (sdk != null) {
            SdkTypeId sdkType = sdk.getSdkType();
            if (sdkType instanceof JavaSdkType) {
                return (JavaSdkType) sdkType;
            }
        }
        return null;
    }

    private Sdk getSdk() {
        return ProjectJdkTable.getInstance().findJdk(myJdkName);
    }

    String getAntHome() {
        File antFolder = new File(PathManager.getLibPath(), "ant");
        return antFolder.getPath();
    }

    private static final String ELEM_FILE = "file";
    private static final String ELEM_TASKS_FOLDER = "tasks-folder";
    private static final String ELEM_VM_PARAMS = "vm-params";
    private static final String ATTR_PORT = "debug-port";
    private static final String ATTR_JDK = "jdk-name";
    private static final String ATTR_TARGET = "target";
    private static final String ATTR_LOGGING_LEVEL = "logging-level";
    private static final String ATTR_MAX_MEMORY = "max-memory";

    @Override
    public void writeExternal(final @NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);

        VirtualFile buildFile = getBuildFile();
        if (buildFile != null) {
            Element file = new Element(ELEM_FILE);
            file.setText(buildFile.getPath());
            element.addContent(file);
        }

        if (myTasksFolder != null) {
            Element file = new Element(ELEM_TASKS_FOLDER);
            file.setText(myTasksFolder.getPath());
            element.addContent(file);
        }

        if (myVmParameters != null) {
            Element vmParams = new Element(ELEM_VM_PARAMS);
            vmParams.setText(myVmParameters);
            element.addContent(vmParams);
        }

        element.setAttribute(ATTR_PORT, Integer.toString(myDebugPort));
        if (myJdkName != null) {
            element.setAttribute(ATTR_JDK, myJdkName);
        }
        if (myTargetName != null) {
            element.setAttribute(ATTR_TARGET, myTargetName);
        }
        element.setAttribute(ATTR_MAX_MEMORY, Integer.toString(myMaxMemory));
        element.setAttribute(ATTR_LOGGING_LEVEL, Integer.toString(getLoggingLevel().getValue()));
    }

    @Override
    public void readExternal(final @NotNull Element element) throws InvalidDataException {
        super.readExternal(element);

        Element file = element.getChild(ELEM_FILE);
        if (file != null) {
            String path = file.getText();
            VirtualFile buildFile = FileUtil.findFile(path);
            setBuildFile(buildFile);
        }

        Element tasksFolderElem = element.getChild(ELEM_TASKS_FOLDER);
        if (tasksFolderElem != null) {
            String path = tasksFolderElem.getText();
            VirtualFile folder = FileUtil.findFile(path);
            setTasksFolder(folder);
        }
        Element vmParamsElem = element.getChild(ELEM_VM_PARAMS);
        if (vmParamsElem != null) {
            String vmParams = vmParamsElem.getText();
            if (vmParams == null || !vmParams.isEmpty()) {
                myVmParameters = vmParams;
            }
        }

        setDebugPort(XmlUtil.getIntAttribute(element, ATTR_PORT, DEFAULT_DEBUG_PORT));
        setMaxMemory(XmlUtil.getIntAttribute(element, ATTR_MAX_MEMORY, DEFAULT_MAX_MEMORY));

        myJdkName = element.getAttributeValue(ATTR_JDK);
        myTargetName = element.getAttributeValue(ATTR_TARGET);
        String levelStr = element.getAttributeValue(ATTR_LOGGING_LEVEL);
        int level = 0;
        if (levelStr != null) {
            try {
                level = Integer.parseInt(levelStr);
            } catch (final NumberFormatException ignored) {
            }
        }
        myLoggingLevel = AntLoggingLevel.get(level);
    }

    String getJdkName() {
        return myJdkName;
    }

    void setJdkName(final String jdkName) {
        myJdkName = jdkName;
    }

    int getMaxMemory() {
        return myMaxMemory;
    }

    void setMaxMemory(final int maxMemory) {
        myMaxMemory = maxMemory;
    }

    String getTargetName() {
        return myTargetName;
    }

    void setTargetName(final String targetName) {
        myTargetName = targetName;
    }

    String getVmParameters() {
        return myVmParameters;
    }

    void setVmParameters(final String vmParameters) {
        myVmParameters = vmParameters;
    }

    AntLoggingLevel getLoggingLevel() {
        return myLoggingLevel != null
                ? myLoggingLevel
                : AntLoggingLevel.DEFAULT;
    }

    void setLoggingLevel(final AntLoggingLevel loggingLevel) {
        myLoggingLevel = loggingLevel;
    }
}
