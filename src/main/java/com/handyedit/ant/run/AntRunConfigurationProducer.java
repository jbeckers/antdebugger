package com.handyedit.ant.run;

import com.handyedit.ant.util.AntUtil;
import com.handyedit.ant.util.IdeaConfigUtil;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AntRunConfigurationProducer extends LazyRunConfigurationProducer<AntRunConfiguration> {

    @Override
    protected boolean setupConfigurationFromContext(final @NotNull AntRunConfiguration configuration,
                                                    final @NotNull ConfigurationContext context,
                                                    final @NotNull Ref<PsiElement> sourceElement) {
        String target = AntUtil.getTarget(context.getPsiLocation());
        configuration.setName(target);
        configuration.setTargetName(target);
        configuration.setBuildFile(context.getLocation().getVirtualFile());
        Sdk targetJdk = IdeaConfigUtil.getJdk(context.getLocation().getModule(), context.getLocation().getProject());
        if (targetJdk != null) {
            configuration.setJdkName(targetJdk.getName());
        }
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(final @NotNull AntRunConfiguration configuration,
                                              final @NotNull ConfigurationContext context) {
        return Objects.equals(AntUtil.getTarget(context.getPsiLocation()), configuration.getTargetName());
    }

    @Override
    public boolean isPreferredConfiguration(final ConfigurationFromContext self,
                                            final ConfigurationFromContext other) {
        return other.isProducedBy(AntRunConfigurationProducer.class);
    }

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return new AntRunConfigurationFactory(new AntRunConfigurationType());
    }

}
