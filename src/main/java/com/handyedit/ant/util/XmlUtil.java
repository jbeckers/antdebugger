package com.handyedit.ant.util;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author Alexei Orischenko
 * Date: Nov 7, 2009
 */
public final class XmlUtil {

    private XmlUtil() {
    }

    public static @Nullable XmlTag getTag(@NotNull final XmlFile file,
                                          final int line) {
        Document doc = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        XmlDocument xmlDoc = file.getDocument();
        if (doc == null || xmlDoc == null) {
            return null;
        }

        int startOffset = doc.getLineStartOffset(line);
        int endOffset = doc.getLineEndOffset(line);
        PsiElement elem = xmlDoc.findElementAt(startOffset);

        XmlTag tag = PsiTreeUtil.getParentOfType(elem, XmlTag.class, false);
        if (tag == null) {
            return null;
        }

        for (final XmlTag child : tag.getSubTags()) {
            int childOffset = child.getTextOffset();
            if (startOffset <= childOffset && childOffset <= endOffset) {
                return child;
            }
        }
        return tag;
    }

    public static int getIntAttribute(final Element elem,
                                      final String name,
                                      final int defaultValue) {
        String val = elem.getAttributeValue(name);
        if (val == null || val.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    public static XmlToken getStartTagEnd(final PsiElement tag) {
        return tag == null
                ? null
                : Arrays.stream(tag.getChildren())
                        .filter(XmlToken.class::isInstance)
                        .map(XmlToken.class::cast)
                        .filter(token ->
                                XmlTokenType.XML_TAG_END.equals(token.getTokenType())
                                        || XmlTokenType.XML_EMPTY_ELEMENT_END.equals(token.getTokenType()))
                        .findFirst()
                        .orElse(null);

    }
}
