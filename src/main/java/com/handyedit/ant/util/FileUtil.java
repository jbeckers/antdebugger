package com.handyedit.ant.util;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Alexei Orischenko
 * Date: Nov 10, 2009
 */
public final class FileUtil {

    private static final String FILE_URL_PREFIX = "file:///";

    private FileUtil() {
    }

    public static void addClasspath(final String path,
                                    final @NotNull StringBuffer result) {
        result.append(path);
        result.append(File.pathSeparator);
    }

    @Contract(pure = true)
    public static @NotNull String getPath(final String parent,
                                          final String child) {
        return parent + File.separator + child;
    }

    public static VirtualFile findFile(final String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        return path.startsWith(FILE_URL_PREFIX)
                ? VirtualFileManager.getInstance().findFileByUrl(path)
                : VirtualFileManager.getInstance().findFileByUrl(FILE_URL_PREFIX + path);

    }

    static @Nullable String getAbsolutePath(final String path,
                                            final String folder) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        return isAbsolutePath(path)
                ? path
                : getPath(folder, path);
    }

    private static boolean isAbsolutePath(final String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }

        if (SystemInfo.isWindows) {
            if (path.length() >= 3) {
                String prefix = path.substring(0, 3);
                return Character.isLetter(prefix.charAt(0)) && (
                        prefix.endsWith(":/") || prefix.endsWith(":\\"));
            }
        } else {
            return path.startsWith("/");
        }

        return false;
    }
}
