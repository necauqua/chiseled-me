/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.size;

import dev.necauqua.mods.cm.ChiseledMe;
import dev.necauqua.mods.cm.ChiseledMe.*;
import dev.necauqua.mods.cm.Config;
import dev.necauqua.mods.cm.Log;
import dev.necauqua.mods.cm.SidedHandler;
import dev.necauqua.mods.cm.api.IRenderSized;
import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

import static dev.necauqua.mods.cm.ChiseledMe.*;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;

@EventBusSubscriber(modid = MODID)
public final class EntitySizeInteractions {

    private static final String NBT_KEY_SIZE = MODID + ":size";

    private EntitySizeInteractions() {}

    @Init
    private static void fixBedAABB() {
        if (!Config.changeBedAABB) {
            return;
        }
        try {
            EnumHelper.setFailsafeFieldValue(
                    ObfuscationReflectionHelper.findField(BlockBed.class, "field_185513_c"),
                    null,
                    new AxisAlignedBB(0.0, 0.1875, 0.0, 1.0, 0.5625, 1.0));
            // ^ this can set final non-primitive fields
        } catch (Exception e) {
            Log.error("Failed to modify bed AABB!", e);
        }
    }

    @SubscribeEvent
    public static void on(EntityInteractSpecific e) {
        if (((ISized) e.getEntity()).getSizeCM() != ((ISized) e.getTarget()).getSizeCM()) {
            e.setCanceled(true);
        }
        for (EnumHand hand : EnumHand.values()) {
            if (e.getEntityPlayer().getHeldItem(hand).getItem() == ChiseledMe.RECALIBRATOR) {
                e.setCanceled(true);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void on(EntityInteract e) {
        for (EnumHand hand : EnumHand.values()) {
            if (e.getEntityPlayer().getHeldItem(hand).getItem() == ChiseledMe.RECALIBRATOR) {
                e.setCanceled(true);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void on(EntityMountEvent e) {
        if (Config.allowAnyRiding || !e.isMounting()) {
            return;
        }
        double mountingSize = ((ISized) e.getEntityMounting()).getSizeCM();
        double mountedSize = e.getEntityBeingMounted() != null ? ((ISized) e.getEntityBeingMounted()).getSizeCM() : 1.0;

        if ((!Config.allowRidingSameSize || mountingSize != mountedSize) && (mountingSize != 1.0 || mountedSize != 1.0)) {
            e.setCanceled(true);
        }
    }

    private static final SleepResult TOO_SMALL = EnumHelper.addStatus("TOO_SMALL");
    private static final SleepResult TOO_BIG = EnumHelper.addStatus("TOO_BIG");

    @SubscribeEvent
    public static void on(PlayerSleepInBedEvent e) {
        EntityPlayer player = e.getEntityPlayer();
        double size = ((ISized) player).getSizeCM();
        if (size < 1.0 && !Config.allowSleepingWhenSmall) {
            e.setResult(TOO_SMALL);
            player.sendMessage(new TextComponentTranslation("chiseled_me.bed.too_small"));
        } else if (size > 1.0 && !Config.allowSleepingWhenBig) {
            e.setResult(TOO_BIG);
            player.sendMessage(new TextComponentTranslation("chiseled_me.bed.too_big"));
        }
    }

    @SubscribeEvent
    public static void on(LivingDropsEvent e) {
        double size = ((ISized) e.getEntity()).getSizeCM();
        if (size == 1.0) {
            return;
        }
        for (EntityItem item : e.getDrops()) {
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

    private static void applyDefaultSize(Entity entity, String entitySizeRule) {
        try {
            double size = MathHelper.clamp(Double.parseDouble(entity.world.getGameRules().getString(entitySizeRule)), LOWER_LIMIT, UPPER_LIMIT);
            if (size != 1.0 && ((ISized) entity).getSizeCM() == 1.0) {
                ((ISized) entity).setSizeCM(size);
            }
        } catch (NumberFormatException ignored) {}
    }

    @SubscribeEvent
    public static void on(PlayerEvent.Clone e) {
        if (e.isWasDeath() && e.getEntityPlayer().world.getGameRules().getBoolean(KEEP_SIZE_RULE)) {
            ((ISized) e.getEntityPlayer()).setSizeCM(((ISized) e.getOriginal()).getSizeCM());
        }
    }

    @SubscribeEvent
    public static void on(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof EntityPlayer) {
            applyDefaultSize(entity, PLAYER_SIZE_RULE);
        } else {
            applyDefaultSize(entity, ENTITY_SIZE_RULE);
        }

        if (entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) entity).getItem();
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey(NBT_KEY_SIZE, 6)) {
                ((ISized) entity).setSizeCM(nbt.getDouble(NBT_KEY_SIZE));
                nbt.removeTag(NBT_KEY_SIZE);
                if (nbt.hasNoTags()) {
                    stack.setTagCompound(null);
                }
            }
            return;
        }

        Entity thrower;
        if (entity instanceof EntityThrowable) {
            thrower = ((EntityThrowable) entity).getThrower();
        } else if (entity instanceof EntityArrow) {
            thrower = ((EntityArrow) entity).shootingEntity;
        } else if (entity instanceof EntityFireball) {
            thrower = ((EntityFireball) entity).shootingEntity;
        } else if (entity instanceof IThrowableEntity) {
            thrower = ((IThrowableEntity) entity).getThrower();
        } else {
            return;
        }
        if (thrower == null) {
            return;
        }
        double size = ((ISized) thrower).getSizeCM();
        if (size != 1.0) {
            ((ISized) entity).setSizeCM(size);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void on(PlayerEvent.BreakSpeed e) {
        e.setNewSpeed((float) (e.getNewSpeed() * ((ISized) e.getEntity()).getSizeCM()));
    }

    @SubscribeEvent
    public static void on(RenderGameOverlayEvent.Text e) {
        EntityPlayer player = SidedHandler.instance.getClientPlayer();
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

    // apparently you cannot reference EntityPlayer from Entity mixin idk
    public static void wakeUp(Entity entity) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isPlayerSleeping()) {
            ((EntityPlayer) entity).wakeUpPlayer(true, !entity.world.isRemote, true);
        }
    }

    public static double getAverageSize(Object a, Object b) {
        double sa = ((ISized) a).getSizeCM();
        double sb = ((ISized) b).getSizeCM();
        return sa == sb ? sa : Math.sqrt(sa * sb);
    }

    @SideOnly(Side.CLIENT)
    public static double getViewerSize() {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        return viewer != null ?
                ((ISized) viewer).getSizeCM() :
                1.0;
    }

    @SideOnly(Side.CLIENT)
    public static double getViewerSize(float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        return viewer != null ?
                ((IRenderSized) viewer).getSizeCM(partialTicks) :
                1.0;
    }

    public static int[] appendSize(int[] array, double d) {
        if (d == 1.0) {
            return array;
        }
        int[] modified = Arrays.copyOf(array, array.length + 2);
        long bits = Double.doubleToRawLongBits(d);
        modified[array.length] = (int) (bits >> 32);
        modified[array.length + 1] = (int) bits;
        return modified;
    }

    public static double extractSize(int particleId, int[] parameters) {
        EnumParticleTypes type = EnumParticleTypes.getParticleFromId(particleId);
        if (type == null) {
            return 1.0;
        }
        int offset;
        int count = type.getArgumentCount();
        if (parameters.length - count == 2) {
            offset = count;
        } else if (type == EnumParticleTypes.ITEM_CRACK && parameters.length > 2) {
            offset = 1;
        } else {
            return 1.0;
        }
        return Double.longBitsToDouble((long) parameters[offset] << 32 | parameters[offset + 1] & 0xFFFFFFFFL);
    }
}
