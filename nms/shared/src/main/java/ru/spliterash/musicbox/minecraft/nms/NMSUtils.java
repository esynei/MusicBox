package ru.spliterash.musicbox.minecraft.nms;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NMSUtils {
    private static final Pattern VERSION_PREFIX = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private static final Pattern SERVER_VERSION = Pattern.compile("\\(MC:\\s*(\\d+\\.\\d+(?:\\.\\d+)?)\\)");
    private NMSUtils() {}

    public static int parseMajorVersion(String raw) {
        Matcher matcher = VERSION_PREFIX.matcher(raw.trim());
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse Minecraft version: " + raw);
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));

        // old numbering: 1.21.11 -> compatibility vers 21
        // new numbering: 26.3 -> compatibility vers 26
        return major == 1 ? minor : major;
    }

    public static String getRawVersion() {
        Matcher bukkitVersion = VERSION_PREFIX.matcher(Bukkit.getBukkitVersion());
        if (bukkitVersion.find()) return bukkitVersion.group(0);

        Matcher serverVersion = SERVER_VERSION.matcher(Bukkit.getVersion());
        if (serverVersion.find()) return serverVersion.group(1);

        throw new IllegalStateException("Cannot detect Minecraft version from Bukkit: " + Bukkit.getVersion());
    }

    public static int getVersion() {
        return parseMajorVersion(getRawVersion());
    }
}
