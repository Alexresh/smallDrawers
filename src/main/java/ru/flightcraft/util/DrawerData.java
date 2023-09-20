package ru.flightcraft.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class DrawerData {
    private static final String countKey = "count";
    private static final String maxCountKey = "maxCount";
    private static final String isDrawerKey = "isDrawer";
    private static final String usersKey = "users";
    private static final String ownerKey = "owner";
    private static final String lockKey = "lock";
    private static final String upgradesKey = "upgrades";
    private static final int smallSize = 2048;
    private static final int mediumSize = 4096;
    private static final int largeSize = 8192;


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

    public static boolean upgrade(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        int maxCount = nbtCompound.getInt(maxCountKey);
        switch (maxCount){
            case smallSize -> nbtCompound.putInt(maxCountKey, mediumSize);
            case mediumSize -> nbtCompound.putInt(maxCountKey, largeSize);
            case largeSize -> {
                return false;
            }
        }
        nbtCompound.putInt(upgradesKey, nbtCompound.getInt(upgradesKey) + 1);
        return true;
    }

    public static int getUpgrades(IEntityNBTSaver frame){
        return frame.getPersistentData().getInt(upgradesKey);
    }

    public static void changeToItemFrame(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        nbtCompound.putBoolean(isDrawerKey, false);
    }

    public static boolean isDrawer(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        return nbtCompound.getBoolean(isDrawerKey);
    }

    public static void onUse(IEntityNBTSaver frame, PlayerEntity player){
        NbtCompound nbtCompound = frame.getPersistentData();
        NbtList users = getUsers(frame);
        if(!users.contains(NbtString.of(player.getName().getString())))
            users.add(NbtString.of(player.getName().getString()));
        nbtCompound.put(usersKey, users);
    }

    public static NbtList getUsers(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        return nbtCompound.getList(usersKey, NbtElement.STRING_TYPE);
    }

    public static void setOwner(IEntityNBTSaver frame, PlayerEntity player){
        NbtCompound nbtCompound = frame.getPersistentData();
        nbtCompound.putString(ownerKey, player.getName().getString());
    }

    public static String getOwner(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        return nbtCompound.getString(ownerKey);
    }

    public static void setLock(IEntityNBTSaver frame, boolean lock){
        NbtCompound nbtCompound = frame.getPersistentData();
        nbtCompound.putBoolean(lockKey, lock);
    }

    public static boolean getLock(IEntityNBTSaver frame){
        NbtCompound nbtCompound = frame.getPersistentData();
        return nbtCompound.getBoolean(lockKey);
    }

    public static boolean canUse(IEntityNBTSaver frame, PlayerEntity player){
        if(!getLock(frame)) return true;
        if(getOwner(frame).equals(player.getName().getString())) return true;
        if(player.hasPermissionLevel(2)) return true;
        return getUsers(frame).contains(NbtString.of(player.getName().getString()));
    }

}
