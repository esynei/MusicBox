package ru.spliterash.musicbox.minecraft.nms.jukebox;

import org.bukkit.block.Jukebox;
import ru.spliterash.musicbox.minecraft.nms.NMSUtils;

import java.lang.reflect.InvocationTargetException;

public class JukeboxFactory {
    private static final String START_PATH = "ru.spliterash.musicbox.minecraft.nms.jukebox.versions.";
    private static final Class<? extends IJukebox> clazz;

    static {
        String raw = NMSUtils.getRawVersion();
        int version = NMSUtils.parseMajorVersion(raw);
        String className;
        if (version >= 13) className = "ru.spliterash.musicbox.minecraft.nms.jukebox.BukkitJukebox";
        else if (version == 12) className = START_PATH + "V12";
        else className = null;
        if (className == null) {
            throw new IllegalArgumentException("Unsupported version: " + raw);
        }

        Class<? extends IJukebox> tmpClass = null;
        try {
            //noinspection unchecked
            tmpClass = (Class<? extends IJukebox>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            clazz = tmpClass;
        }
    }

    public static boolean jukeboxAvailable() {
        return clazz != null;
    }

    public static IJukebox getJukebox(Jukebox jukebox) {
        try {
            return clazz.getConstructor(Jukebox.class).newInstance(jukebox);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
