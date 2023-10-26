/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.size;

import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.Mira;
import dev.necauqua.mods.mira.api.ISized;
import dev.necauqua.mods.mira.data.DoubleValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepResult;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.List;

import static dev.necauqua.mods.mira.Mira.MODID;

@EventBusSubscriber(modid = MODID)
public final class ForgeHandlers {

    private static final String NBT_KEY_SIZE = MODID + ":size";

    private ForgeHandlers() {
    }

    // @SubscribeEvent
    // public static void on(EntityInteractSpecific e) {
    // if (((ISized) e.getEntity()).getSizeCM() != ((ISized)
    // e.getTarget()).getSizeCM()) {
    // e.setCanceled(true);
    // }
    // for (Hand hand : Hand.values()) {
    // if (e.getPlayer().getHeldItem(hand).getItem() == ChiseledMe.RECALIBRATOR) {
    // e.setCanceled(true);
    // break;
    // }
    // }
    // }

    // @SubscribeEvent
    // public static void on(EntityInteract e) {
    // for (Hand hand : Hand.values()) {
    // if (e.getPlayer().getHeldItem(hand).getItem() == ChiseledMe.RECALIBRATOR) {
    // e.setCanceled(true);
    // break;
    // }
    // }
    // }

    @SubscribeEvent
    public static void on(EntityMountEvent e) {
        if (Config.allowAnyRiding.get() || !e.isMounting()) {
            return;
        }
        double mountingSize = ((ISized) e.getEntityMounting()).getSizeCM();
        double mountedSize = e.getEntityBeingMounted() != null ? ((ISized) e.getEntityBeingMounted()).getSizeCM() : 1.0;

        if ((!Config.allowRidingSameSize.get() || mountingSize != mountedSize)
            && (mountingSize != 1.0 || mountedSize != 1.0)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void on(PlayerSleepInBedEvent e) {
        PlayerEntity player = e.getPlayer();
        double size = ((ISized) player).getSizeCM();
        if (size < 1.0 && !Config.allowSleepingWhenSmall.get()) {
            e.setResult(SleepResult.OTHER_PROBLEM);
            player.sendMessage(new TranslationTextComponent("mira.bed.too_small"), Util.NIL_UUID);
        } else if (size > 1.0 && !Config.allowSleepingWhenBig.get()) {
            e.setResult(SleepResult.OTHER_PROBLEM);
            player.sendMessage(new TranslationTextComponent("mira.bed.too_big"), Util.NIL_UUID);
        }
    }

    @SubscribeEvent
    public static void on(LivingDropsEvent e) {
        double size = ((ISized) e.getEntity()).getSizeCM();
        if (size == 1.0) {
            return;
        }
        for (ItemEntity item : e.getDrops()) {
            ((ISized) item).setSizeCM(size);
        }
    }

    @SubscribeEvent
    public static void on(ItemTossEvent e) {
        double size = ((ISized) e.getPlayer()).getSizeCM();
        if (size != 1.0) {
            ((ISized) e.getEntity()).setSizeCM(size);
        }
    }

    @SubscribeEvent
    public static void on(PlayerEvent.Clone e) {
        if (e.isWasDeath() && e.getPlayer().level.getGameRules().getBoolean(Mira.KEEP_SIZE_RULE)) {
            ((ISized) e.getPlayer()).setSizeCM(((ISized) e.getOriginal()).getSizeCM());
        }
    }

    @SubscribeEvent
    public static void on(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof PlayerEntity) {
            applyDefaultSize(entity, Mira.PLAYER_SIZE_RULE);
        } else {
            applyDefaultSize(entity, Mira.ENTITY_SIZE_RULE);
        }

        if (entity instanceof ItemEntity) {
            ItemStack stack = ((ItemEntity) entity).getItem();
            CompoundNBT nbt = stack.getTag();
            if (nbt == null || !nbt.contains(NBT_KEY_SIZE, NBT.TAG_DOUBLE)) {
                return;
            }
            ((ISized) entity).setSizeCM(nbt.getDouble(NBT_KEY_SIZE));
            nbt.remove(NBT_KEY_SIZE);
            if (nbt.isEmpty()) {
                stack.setTag(null);
            }
            return;
        }

        if (!(entity instanceof ProjectileEntity)) {
            return;
        }
        Entity thrower = ((ProjectileEntity) entity).getOwner();
        if (thrower == null) {
            return;
        }
        double size = ((ISized) thrower).getSizeCM();
        if (size != 1.0) {
            ((ISized) entity).setSizeCM(size);
        }
    }

    private static void applyDefaultSize(Entity entity, RuleKey<DoubleValue> rule) {
        try {
            double size = MathHelper.clamp(entity.level.getGameRules().getRule(rule).get(), Mira.LOWER_LIMIT,
                Mira.UPPER_LIMIT);
            if (size != 1.0 && ((ISized) entity).getSizeCM() == 1.0) {
                ((ISized) entity).setSizeCM(size);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void on(PlayerEvent.BreakSpeed e) {
        e.setNewSpeed((float) (e.getNewSpeed() * ((ISized) e.getEntity()).getSizeCM()));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void on(RenderGameOverlayEvent.Text e) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        double size = ((ISized) player).getSizeCM();
        if (size == 1.0) {
            return;
        }
        List<String> list = e.getLeft();
        if (list.size() >= 3) {
            list.add(list.size() - 3, String.format("Size: %f", size));
        }
    }

    @SubscribeEvent
    public static void on(BabyEntitySpawnEvent e) {
        double size = ((ISized) e.getParentA()).getSizeCM();
        Entity child = e.getChild();
        if (child != null && size != 1.0) {
            ((ISized) child).setSizeCM(size);
        }
    }
}
