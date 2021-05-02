package me.geek.tom.nucleoidextrasvelocity;

import com.velocitypowered.api.util.ProxyVersion;

public class Util {
    public static void verifyThat(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static String formatProxyVersion(ProxyVersion version) {
        return version.getName() + " " + version.getVersion();
    }
}
