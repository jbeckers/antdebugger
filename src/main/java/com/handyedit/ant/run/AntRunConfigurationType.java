package com.handyedit.ant.run;

import javax.swing.*;

import com.handyedit.ant.util.AntUtil;
import com.handyedit.ant.util.IdeaConfigUtil;
import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import icons.AntIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntRunConfigurationType implements ConfigurationType {

    private ConfigurationFactory myFactory;

    @Override
    public @NotNull String getDisplayName() {
        return "Ant Build";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Ant build";
    }

    @Override
    public Icon getIcon() {
        return Nodes.Target;
    }

    @Override
    @NotNull
    public String getId() {
        return "AntRunConfigurationType";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        if (myFactory == null) {
            myFactory = new AntRunConfigurationFactory(this);
        }
        return new ConfigurationFactory[]{myFactory};
    }

    public RunnerAndConfigurationSettings createConfigurationByLocation(final Location location) {
        OpenFileDescriptor fileDescriptor = location.getOpenFileDescriptor();
        VirtualFile file = fileDescriptor != null ? fileDescriptor.getFile() : null;
        if (file == null || !(file.getFileType() instanceof XmlFileType)) {
            return null;
        }

        String target = getTarget(location);

        String name = target != null ? target : file.getNameWithoutExtension();

        RunnerAndConfigurationSettings settings =
                RunManager.getInstance(location.getProject()).createConfiguration(name, myFactory);

        AntRunConfiguration templateConfiguration = (AntRunConfiguration) settings.getConfiguration();

        Module module = ModuleUtilCore.findModuleForFile(file, location.getProject());
        templateConfiguration.setModule(module);

        templateConfiguration.setBuildFile(file);
        Sdk jdk = IdeaConfigUtil.getJdk(module, location.getProject());
        templateConfiguration.setJdkName(jdk != null ? jdk.getName() : null);

        templateConfiguration.setTargetName(target);
        templateConfiguration.setName(name);

        AntUtil.disableCompileBeforeRun(templateConfiguration);

        return settings;
    }

    public boolean isConfigurationByLocation(final RunConfiguration runConfiguration, final Location location) {
        if (runConfiguration instanceof AntRunConfiguration) {
            AntRunConfiguration config = (AntRunConfiguration) runConfiguration;
            OpenFileDescriptor fileDescriptor = location.getOpenFileDescriptor();
            VirtualFile file = fileDescriptor != null ? fileDescriptor.getFile() : null;

          return Objects.equals(config.getBuildFile(), file) &&
                  Objects.equals(config.getTargetName(), getTarget(location));
        }
        return false;
    }

    private static String getTarget(final Location location) {
        if (location == null) {
          return null;
        }

        PsiElement elem = location.getPsiElement();
        while (elem != null) {
              //TODO: This compiles but doesn't always return the right value
              if (elem instanceof XmlText) {
                  XmlText target = (XmlText) elem;

                  XmlTag parentTag = target.getParentTag();
                  XmlAttribute targetName = parentTag.getAttribute("name");
                  return targetName.getDisplayValue();
              }
              elem = elem.getParent();
        }
        return null;
    }
}
