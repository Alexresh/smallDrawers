package ru.flightcraft.util;

import net.minecraft.nbt.NbtCompound;

public class DrawerData {
    private static final String countKey = "count";
    private static final String maxCountKey = "maxCount";
    private static final String isDrawerKey = "isDrawer";
    private static final int smallSize = 2048;


    public static int addItem(IEntityNBTSaver frame, int amount){
        NbtCompound nbtCompound = frame.getPersistentData();
        int oversize = 0;
        int count = nbtCompound.getInt(countKey);
        int maxCount = nbtCompound.getInt(maxCountKey);
        if(count + amount > maxCount){
            oversize = count + amount - maxCount;
            count = maxCount;
        }else {
            count += amount;
        }

        nbtCompound.putInt(countKey, count);
        return oversize;
    }

    public static boolean removeItem(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        int count = getCount(frame);
        if(count < 1) return false;
        nbtCompound.putInt(countKey, count - 1);
        return true;
    }
    public static int removeMax(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        int count = getCount(frame);
        int returnedCount;
        if(count >= 64){
            returnedCount = 64;
            nbtCompound.putInt(countKey, count - 64);
        }else{
            returnedCount = count;
            nbtCompound.putInt(countKey, 0);
        }
        return returnedCount;
    }

    public static int getFreeSpace(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        int maxCount = nbtCompound.getInt(maxCountKey);
        return maxCount - getCount(frame);
    }

    public static int getCount(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        return nbtCompound.getInt(countKey);
    }

    public static void changeToDrawer(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        nbtCompound.putBoolean(isDrawerKey, true);
        nbtCompound.putInt(maxCountKey, smallSize);
    }
    public static void changeToItemFrame(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        nbtCompound.putBoolean(isDrawerKey, false);
    }

    public static boolean isDrawer(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        return nbtCompound.getBoolean(isDrawerKey);
    }


}
