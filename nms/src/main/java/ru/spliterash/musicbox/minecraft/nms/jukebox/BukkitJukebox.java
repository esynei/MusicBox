package ru.spliterash.musicbox.minecraft.nms.jukebox;

import org.bukkit.block.Jukebox;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BukkitJukebox implements IJukebox {
    private final Jukebox jukebox;

    public BukkitJukebox(Jukebox jukebox) {
        this.jukebox = jukebox;
    }

    @Override
    public void setJukebox(ItemStack item) {
        try {
            Object tileEntity = getTileEntity();
            Object nmsItem = toNmsItem(item);

            setWithoutPlaying(tileEntity, nmsItem);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not update jukebox without vanilla playback at " + jukebox.getLocation(), e);
        }
    }

    @Override
    public ItemStack getJukebox() {
        if (!(jukebox.getBlock().getState() instanceof Jukebox)) return null;

        Jukebox current = (Jukebox) jukebox.getBlock().getState();
        return current.getRecord();
    }

    private Object toNmsItem(ItemStack item) throws ReflectiveOperationException {
        String blockPackage = jukebox.getClass().getPackage().getName();
        String craftPackage = blockPackage.substring(0, blockPackage.length() - ".block".length());
        Class<?> craftItemStack = Class.forName(craftPackage + ".inventory.CraftItemStack");
        return craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
    }

    private Object getTileEntity() throws ReflectiveOperationException {
        Method getter = findMethod(jukebox.getClass(), "getBlockEntity");
        if (getter == null) getter = findMethod(jukebox.getClass(), "getTileEntity");
        if (getter != null) return getter.invoke(jukebox);

        Class<?> type = jukebox.getClass();
        while (type != null) {
            for (String fieldName : new String[]{"blockEntity", "tileEntity"}) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(jukebox);
                } catch (NoSuchFieldException ignored) {

                }
            }
            type = type.getSuperclass();
        }

        throw new NoSuchFieldException("Could not find jukebox tile entity");
    }

    private void setWithoutPlaying(Object tileEntity, Object nmsItem) throws ReflectiveOperationException {
        Class<?> type = tileEntity.getClass();
        Class<?> itemType = nmsItem.getClass();
        Method method = findMethod(type, "setSongItemWithoutPlaying", itemType);
        if (method != null) {
            method.invoke(tileEntity, nmsItem);
            return;
        }

        method = findMethod(type, "setSongItemWithoutPlaying", itemType, long.class);
        if (method != null) {
            method.invoke(tileEntity, nmsItem, 0L);
            return;
        }

        method = findMethod(type, "setSongItemWithoutPlaying", itemType, int.class);
        if (method != null) {
            method.invoke(tileEntity, nmsItem, 0);
            return;
        }

        for (String methodName : new String[]{"setRecordWithoutPlaying", "setRecord", "a"}) {
            method = findMethod(type, methodName, itemType);
            if (method != null) {
                method.invoke(tileEntity, nmsItem);
                return;
            }
        }

        throw new NoSuchMethodException("No supported no-play method found in " + type.getName());
    }

    private Method findMethod(Class<?> type, String name, Class<?>... parameters) {
        try {
            return type.getMethod(name, parameters);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}