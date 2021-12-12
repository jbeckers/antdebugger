package com.handyedit.ant.util;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.compiler.options.CompileStepBeforeRun.MakeBeforeRunTask;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alexei Orischenko
 * Date: Dec 14, 2009
 */
public final class AntUtil {

    private static final String TAG_TARGET = "target";
    private static final String ATTR_NAME = "name";

    private static final String TAG_IMPORT = "import";
    private static final String ATTR_FILE = "file";

    private AntUtil() {
    }

    public static @NotNull Set<String> getTargets(final VirtualFile antFile,
                                                  final Project project) {
        Set<String> result = new HashSet<String>();

        if (antFile == null) {
            return result;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(antFile);
        if (!(psiFile instanceof XmlFile)) {
            return result;
        }
        XmlFile xmlFile = (XmlFile) psiFile;

        XmlDocument xmlDoc = xmlFile.getDocument();
        if (xmlDoc == null) {
            return result;
        }

        XmlTag root = xmlDoc.getRootTag();
        if (root != null) {
            for (final XmlTag child : root.getSubTags()) {
                String tagName = child.getName();
                if (TAG_TARGET.equals(tagName)) {
                    String name = child.getAttributeValue(ATTR_NAME);
                    if (!StringUtils.isEmpty(name)) {
                        result.add(name);
                    }
                } else if (TAG_IMPORT.equals(tagName)) {
                    String path = child.getAttributeValue(ATTR_FILE);
                    VirtualFile antFileFolder = antFile.getParent();
                    if (!StringUtils.isEmpty(path) && antFileFolder != null) {
                        path = FileUtil.getAbsolutePath(path, antFileFolder.getPath());
                        VirtualFile importedFile = FileUtil.findFile(path);
                        result.addAll(getTargets(importedFile, project));
                    }
                }
            }
        }

        return result;
    }

    public static @Nullable String getTarget(final PsiElement elem) {
        XmlTag tag = PsiTreeUtil.getParentOfType(elem, XmlTag.class);
        while (tag != null) {
            if (TAG_TARGET.equals(tag.getName())) {
                return tag.getAttributeValue(ATTR_NAME);
            } else {
                tag = tag.getParentTag();
            }
        }
        return null;
    }

    public static void disableCompileBeforeRun(final @NotNull RunConfiguration config) {
        RunManagerEx manager = RunManagerEx.getInstanceEx(config.getProject());
        List<MakeBeforeRunTask> tasks = manager.getBeforeRunTasks(config, CompileStepBeforeRun.ID);
        for (final MakeBeforeRunTask beforeRunTask : tasks) {
            beforeRunTask.setEnabled(false);
        }
    }
}
