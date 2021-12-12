package com.handyedit.ant.xdebug;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexei Orischenko
 *         Date: Nov 6, 2009
 */
class AntDebuggerEditorsProvider extends XDebuggerEditorsProvider {

    @NotNull
    @Override
    public FileType getFileType() {
        return XmlFileType.INSTANCE;
    }

    @Override
    public @NotNull Document createDocument(@NotNull final Project project,
                                            @NotNull final XExpression expression,
                                            @Nullable final XSourcePosition sourcePosition,
                                            @NotNull final EvaluationMode mode) {
        PsiFile psiFile = new AntExpressionCodeFragmentImpl(project, "AntDebugger.expr", expression.getExpression());
        return PsiDocumentManager.getInstance(project).getDocument(psiFile);
    }
}
