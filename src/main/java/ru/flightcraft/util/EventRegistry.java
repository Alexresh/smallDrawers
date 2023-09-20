package ru.flightcraft.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EventRegistry {

    public static ActionResult attackEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if(world.isClient) return ActionResult.PASS;
        if(!(entity instanceof ItemFrameEntity itemFrameEntity)) return ActionResult.PASS;
        IEntityNBTSaver frameSaver = (IEntityNBTSaver) itemFrameEntity;
        //if drawer
        if(DrawerData.isDrawer(frameSaver)){
            if(!DrawerData.canUse(frameSaver, player)){
                player.sendMessage(Text.translatable("drawer.locked.by.owner"));
                return ActionResult.SUCCESS;
            }
            DrawerData.onUse(frameSaver, player);

            if(player.isSneaking()){
                int count = DrawerData.removeMax(frameSaver);
                ItemStack insertStack = itemFrameEntity.getHeldItemStack().getItem().getDefaultStack();
                insertStack.setCount(count);
                ItemEntity itemEntity = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), insertStack);
                itemEntity.setPickupDelay(0);
                world.spawnEntity(itemEntity);
            }else{
                if(DrawerData.removeItem(frameSaver)){
                    player.getInventory().insertStack(itemFrameEntity.getHeldItemStack().getItem().getDefaultStack());
                }
            }
            ItemStack held = itemFrameEntity.getHeldItemStack();
            held.setCustomName(Text.literal("" + DrawerData.getCount((frameSaver))));
            itemFrameEntity.setHeldItemStack(held);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static ActionResult useEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if(world.isClient) return ActionResult.PASS;
        if(!(entity instanceof ItemFrameEntity itemFrameEntity)) return ActionResult.PASS;
        if(hitResult == null) return ActionResult.SUCCESS;

        IEntityNBTSaver frameSaver = (IEntityNBTSaver) itemFrameEntity;
        if(!DrawerData.isDrawer(frameSaver)) return ActionResult.PASS;
        if(!DrawerData.canUse(frameSaver, player)){
            player.sendMessage(Text.translatable("drawer.locked.by.owner"));
            return ActionResult.SUCCESS;
        }
        DrawerData.onUse(frameSaver, player);

        ItemStack playerHeldItem = player.getMainHandStack();
        //upgrade
        if(playerHeldItem.isOf(Items.DIAMOND_BLOCK) && playerHeldItem.getNbt() != null){
            if(playerHeldItem.isOf(Items.DIAMOND_BLOCK) && playerHeldItem.getNbt().getCompound("display").getString("Name").contains("upgrade")){
                if(DrawerData.upgrade(frameSaver)){
                    player.sendMessage(Text.translatable("drawer.upgraded"));
                    playerHeldItem.decrement(1);
                }else {
                    player.sendMessage(Text.translatable("drawer.max.upgraded"));
                }
            }
        }
        if(playerHeldItem.isEmpty()){
            if(player.isSneaking()){
                //if player main hand is empty and drawer is empty then delete drawer
                if(DrawerData.getCount(frameSaver) == 0 && DrawerData.getOwner(frameSaver).equals(player.getName().getString())){
                    ItemStack diamond_block = new ItemStack(Items.DIAMOND_BLOCK);
                    diamond_block.setCount(DrawerData.getUpgrades(frameSaver));

                    itemFrameEntity.dropStack(new ItemStack(Items.BARREL));
                    itemFrameEntity.dropStack(new ItemStack(Items.ITEM_FRAME));
                    itemFrameEntity.dropStack(diamond_block);

                    itemFrameEntity.kill();
                    return ActionResult.SUCCESS;
                }
                //insert all stacks from inventory
                Item frameHeldItem = itemFrameEntity.getHeldItemStack().getItem();
                Inventory playerInventory = player.getInventory();
                if(!playerInventory.containsAny(itemStack -> itemStack.getItem() == frameHeldItem)) return ActionResult.SUCCESS;
                int freeSpace = DrawerData.getFreeSpace(frameSaver);
                for (int i = 0; i < playerInventory.size(); i++) {
                    if(playerInventory.getStack(i).getItem() == frameHeldItem){
                        int playerSlotCount = playerInventory.getStack(i).getCount();
                        if(freeSpace >= playerInventory.getStack(i).getCount()){
                            freeSpace -= playerSlotCount;
                            playerInventory.getStack(i).setCount(0);
                            DrawerData.addItem(frameSaver, playerSlotCount);
                        }else{
                            playerInventory.getStack(i).setCount(playerSlotCount - freeSpace);
                            DrawerData.addItem(frameSaver, freeSpace);
                            freeSpace = 0;
                        }
                    }
                }
                itemFrameEntity.setHeldItemStack(itemFrameEntity.getHeldItemStack().setCustomName(Text.literal("" + DrawerData.getCount(frameSaver))));
            }
            return ActionResult.SUCCESS;
        }
        ItemStack itemFrameHeld = itemFrameEntity.getHeldItemStack();
        //adding first item
        if(itemFrameEntity.getHeldItemStack().isEmpty()){
            if(playerHeldItem.getMaxCount() != 64) return ActionResult.SUCCESS;
            DrawerData.addItem(frameSaver, 1);
            playerHeldItem.setCustomName(Text.literal("" + DrawerData.getCount((frameSaver))));
            itemFrameEntity.setHeldItemStack(playerHeldItem);
            player.getMainHandStack().decrement(1);
            itemFrameEntity.setGlowing(false);
            itemFrameEntity.setInvisible(true);
            return ActionResult.SUCCESS;
        }

        if(itemFrameHeld.getItem() == playerHeldItem.getItem()){
            int count = playerHeldItem.getCount();
            //insert items and set oversize count
            playerHeldItem.setCount(DrawerData.addItem(frameSaver, count));
            //update visible count
            itemFrameHeld.setCustomName(Text.literal("" + DrawerData.getCount((frameSaver))));
            itemFrameEntity.setHeldItemStack(itemFrameHeld);
            itemFrameEntity.setRotation(0);
            return ActionResult.SUCCESS;
        }
        //lock/unlock
        if(playerHeldItem.isOf(Items.TRIPWIRE_HOOK) && playerHeldItem.getNbt() != null){
            String name = playerHeldItem.getNbt().getCompound("display").getString("Name");
            if(DrawerData.getOwner(frameSaver).equals(player.getName().getString())){
                if(name.contains("lock")){
                    DrawerData.setLock(frameSaver, !DrawerData.getLock(frameSaver));
                    if(DrawerData.getLock(frameSaver)){
                        player.sendMessage(Text.translatable("drawer.locked").append(DrawerData.getUsers(frameSaver).toString()).formatted(Formatting.GOLD));
                    }else {
                        player.sendMessage(Text.translatable("drawer.unlocked").formatted(Formatting.GOLD));
                    }

                }
            }
        }


        return ActionResult.PASS;
    }

    public static ActionResult attackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if(world.isClient) return ActionResult.PASS;
        BlockState block = world.getBlockState(pos);
        if(block.getBlock() == Blocks.BARREL && player.getMainHandStack().getItem() == Items.ITEM_FRAME){
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            BlockPos framePos = pos.offset(direction);
            ItemFrameEntity itemFrameEntity = new ItemFrameEntity(EntityType.ITEM_FRAME, world, framePos, direction);
            IEntityNBTSaver frameSaver = (IEntityNBTSaver) itemFrameEntity;
            DrawerData.changeToDrawer(frameSaver);
            DrawerData.setOwner(frameSaver, player);

            itemFrameEntity.setInvulnerable(true);
            itemFrameEntity.setGlowing(true);

            world.spawnEntity(itemFrameEntity);
            player.getMainHandStack().decrement(1);

            NbtCompound fixedCompound = new NbtCompound();
            itemFrameEntity.writeCustomDataToNbt(fixedCompound);
            fixedCompound.putBoolean("Fixed", true);
            itemFrameEntity.readCustomDataFromNbt(fixedCompound);

        }

        return ActionResult.PASS;
    }

}
