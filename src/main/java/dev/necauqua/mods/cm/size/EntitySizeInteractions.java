/*
 * Copyright (c) 2016-2019 Anton Bulakh <necauqua@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.necauqua.mods.cm.size;

import dev.necauqua.mods.cm.ChiseledMe.OnInit;
import dev.necauqua.mods.cm.Config;
import dev.necauqua.mods.cm.Log;
import dev.necauqua.mods.cm.SidedHandler;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

import java.util.List;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.asm.dsl.ASM.srg;
import static dev.necauqua.mods.cm.size.EntitySizeManager.getSize;
import static dev.necauqua.mods.cm.size.EntitySizeManager.setSize;

/** This class holds misc event handlers. **/
public final class EntitySizeInteractions {

    private static final String NBT_KEY_SIZE = MODID + ":size";

    private EntitySizeInteractions() {}

    @OnInit
    public static void fixBedAABB() {
        if (!Config.changeBedAABB) {
            return;
        }
        try {
            EnumHelper.setFailsafeFieldValue(
                BlockBed.class.getDeclaredField(srg("BED_AABB")),
                null,
                new AxisAlignedBB(0.0, 0.1875, 0.0, 1.0, 0.5625, 1.0)
            ); // this can set final non-primitive fields
        } catch (Exception e) {
            Log.error("Failed to modify bed AABB!", e);
        }
    }

    @SubscribeEvent
    public static void on(LivingFallEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        double size = getSize(entity);
        if (size == 1.0) {
            return;
        }
        if (size < 1.0 && Config.scaleSmall) {
            event.setDistance((float) (event.getDistance() / size));
        } else if (size > 1.0 && Config.scaleBig) {
            event.setDistance((float) (event.getDistance() / size));
        }
    }

    @SubscribeEvent
    public static void on(EntityInteractSpecific e) {
        if (getSize(e.getEntity()) != getSize(e.getTarget())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void on(EntityMountEvent e) {
        if (e.isMounting() && (getSize(e.getEntityMounting()) != 1.0 || getSize(e.getEntityBeingMounted()) != 1.0)) {
            e.setCanceled(true);
        }
    }

    private static final SleepResult TOO_SMALL = EnumHelper.addStatus("TOO_SMALL");
    private static final SleepResult TOO_BIG = EnumHelper.addStatus("TOO_BIG");

    @SubscribeEvent
    public static void on(PlayerSleepInBedEvent e) {
        EntityPlayer player = e.getEntityPlayer();
        double size = getSize(player);
        if (size < 1.0) {
            e.setResult(TOO_SMALL);
            player.sendMessage(new TextComponentTranslation("chiseled_me.bed.too_small"));
        } else if (size > 1.0) {
            e.setResult(TOO_BIG);
            player.sendMessage(new TextComponentTranslation("chiseled_me.bed.too_big"));
        }
    }

    @SubscribeEvent
    public static void on(LivingDropsEvent e) {
        double size = getSize(e.getEntity());
        if (size == 1.0) {
            return;
        }
        for (EntityItem item : e.getDrops()) {
            setSize(item, size, false);
        }
    }

    @SubscribeEvent
    public static void on(ItemTossEvent e) {
        double size = getSize(e.getPlayer());
        if (size != 1.0) {
            setSize(e.getEntityItem(), size, false);
        }
    }

    @SubscribeEvent
    public static void on(BlockEvent.HarvestDropsEvent e) {
        EntityPlayer player = e.getHarvester();
        if (player == null) {
            return;
        }
        double size = getSize(player);
        for (ItemStack stack : e.getDrops()) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                nbt = new NBTTagCompound();
            }
            nbt.setDouble(NBT_KEY_SIZE, size);
            stack.setTagCompound(nbt);
        }
    }

    @SubscribeEvent
    public static void on(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) entity).getItem();
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey(NBT_KEY_SIZE, 6)) {
                setSize(entity, nbt.getDouble(NBT_KEY_SIZE), false);
                nbt.removeTag(NBT_KEY_SIZE);
                if (nbt.isEmpty()) {
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
        } else if (entity instanceof IThrowableEntity) {
            thrower = ((IThrowableEntity) entity).getThrower();
        } else {
            return;
        }
        if (thrower == null) {
            return;
        }
        double size = getSize(thrower);
        if (size != 1.0) {
            setSize(entity, size, false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void on(PlayerEvent.BreakSpeed e) {
        e.setNewSpeed((float) (e.getNewSpeed() * getSize(e.getEntity())));
    }

    @SubscribeEvent
    public static void on(RenderGameOverlayEvent.Text e) {
        EntityPlayer player = SidedHandler.instance.getClientPlayer();
        if (player == null) {
            return;
        }
        double size = getSize(player);
        if (size != 1.0) {
            List<String> list = e.getLeft();
            if (list.size() >= 3) {
                list.add(list.size() - 3, String.format("Size: %f", size));
            }
        }
    }

    @SubscribeEvent
    public static void on(BabyEntitySpawnEvent e) {
        double size = getSize(e.getParentA());
        if (size != 1.0) {
            setSize(e.getChild(), size, false);
        }
    }
}
