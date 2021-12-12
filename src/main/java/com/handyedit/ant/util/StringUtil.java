package com.handyedit.ant.util;

import java.util.List;

/**
 * @author Alexei Orischenko
 *         Date: Nov 10, 2009
 */
public final class StringUtil {

    public static final String QUOTE = "\"";

    private StringUtil() {
    }

    public static String quote(final String s) {
        return QUOTE + s + QUOTE;
    }

    public static String[] toArray(final List<String> names) {
        if (names == null) {
            return new String[0];
        }

        String[] result = new String[names.size()];
        names.toArray(result);

        return result;
    }

    public static int findPropertyNameEnd(final CharSequence text, int pos, final int increment) {
        if (pos < 0 || pos >= text.length()) {
            return -1;
        }

        char c = text.charAt(pos);
        while (isPropertyNameCharacter(c)) {
            pos += increment;
            if (pos < 0 || pos >= text.length()) {
                return pos;
            }
            c = text.charAt(pos);
        }

        return pos;
    }

    private static boolean isPropertyNameCharacter(final char c) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_';
    }

    public static String removeLineFeeds(final String s) {
        return s == null
                ? null
                : s.replace("\r", "").replace("\n", "");

    }
}
