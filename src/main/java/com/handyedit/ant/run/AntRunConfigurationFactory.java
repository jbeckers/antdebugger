package com.handyedit.ant.run;

import javax.swing.*;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.icons.AllIcons.Nodes;
import com.intellij.openapi.project.Project;
import icons.AntIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntRunConfigurationFactory extends ConfigurationFactory {

    AntRunConfigurationFactory(@NotNull final ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
        return new AntRunConfiguration(project, this, "");
    }

    @Override
    public @NotNull String getName() {
        return "Ant Build";
    }

    @Override
    public Icon getIcon() {
        return Nodes.Target;
    }


}
