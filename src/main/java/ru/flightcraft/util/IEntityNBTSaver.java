package ru.flightcraft.util;

import net.minecraft.nbt.NbtCompound;

public interface IEntityNBTSaver {
    NbtCompound getPersistentData();
}
