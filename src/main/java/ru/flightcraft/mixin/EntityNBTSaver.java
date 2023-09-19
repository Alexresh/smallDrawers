package ru.flightcraft.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.flightcraft.util.IEntityNBTSaver;

@Mixin(Entity.class)
public abstract class EntityNBTSaver implements IEntityNBTSaver {
    private NbtCompound nbtCompound;

    @Override
    public NbtCompound getPersistentData() {
        if(nbtCompound == null) nbtCompound = new NbtCompound();
        return nbtCompound;
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    private void writeCustomNbt(NbtCompound nbt, CallbackInfoReturnable ci){
        if(nbtCompound != null){
            nbt.put("Drawer", nbtCompound);
        }
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    private void readCustomNbt(NbtCompound nbt, CallbackInfo ci){
        if(nbt.contains("Drawer")){
            nbtCompound = nbt.getCompound("Drawer");
        }
    }
}
