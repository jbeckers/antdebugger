package com.handyedit.ant.util;

import com.intellij.openapi.util.SystemInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Alexei Orischenko
 * Date: Nov 10, 2009
 */
public final class NetUtil {

    private NetUtil() {
    }

    public static String getLocalHost() {
        String localHostString = "localhost";
        try {
            InetAddress localHost = InetAddress.getByName(localHostString);
            if (localHost.getAddress().length != 4 && SystemInfo.isWindows) {
                localHostString = "127.0.0.1";
            }
        } catch (final UnknownHostException ignored) {
        }
        return localHostString;
    }

}
