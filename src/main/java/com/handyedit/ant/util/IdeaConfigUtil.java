package com.handyedit.ant.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexei Orischenko
 * Date: Nov 12, 2009
 */
public final class IdeaConfigUtil {

    private IdeaConfigUtil() {
    }

    public static @Nullable Sdk getJdk(@Nullable final Module module,
                                       @NotNull final Project project) {
        if (module != null) {
            Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
            if (moduleSdk != null && moduleSdk.getSdkType() instanceof JavaSdkType) {
                return moduleSdk;
            }
        }

        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null && projectSdk.getSdkType() instanceof JavaSdkType) {
            return projectSdk;
        }

        return ProjectJdkTable.getInstance()
                .getSdksOfType(JavaSdk.getInstance())
                .stream()
                .findFirst()
                .orElse(null);

    }

    /**
     * Returns folder or Jar file with plugin classes.
     *
     * @param pluginClass plugin class
     * @return path to folder or Jar file
     */
    public static @NotNull String getPluginClassesFolder(final Class<?> pluginClass) {
        return PathUtil.getJarPathForClass(pluginClass);
    }
}
