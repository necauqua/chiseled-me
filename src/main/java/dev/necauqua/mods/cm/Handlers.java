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

package dev.necauqua.mods.cm;

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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

import static dev.necauqua.mods.cm.EntitySizeManager.getSize;
import static dev.necauqua.mods.cm.EntitySizeManager.setSize;

/** This class holds misc event handlers. **/
public final class Handlers {

    private Handlers() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new Handlers());
        if (Config.changeBedAABB) {
            fixBedAABB();
        }
    }

    private static void fixBedAABB() {
        try {
            Field f = ReflectionHelper.findField(BlockBed.class, "field_185513_c", "BED_AABB");
            AxisAlignedBB aabb = new AxisAlignedBB(0.0, 0.1875, 0.0, 1.0, 0.5625, 1.0);
            EnumHelper.setFailsafeFieldValue(f, null, aabb); // this can set final non-primitive fields
        } catch (Exception e) {
            Log.error("Failed to modify bed AABB!", e);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
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
        if (thrower != null) {
            float size = getSize(thrower);
            if (size != 1.0F) {
                setSize(entity, size, false);
            }
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        float size = getSize(entity);
        if (size == 1.0F) {
            return;
        }
        if (size < 1.0F && Config.scaleSmall) {
            event.setDamageMultiplier(event.getDamageMultiplier() * size);
            return;
        }
        if (size > 1.0F && Config.scaleBig) {
            event.setDamageMultiplier(event.getDamageMultiplier() * size);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(EntityInteractSpecific e) {
        if (getSize(e.getEntity()) != getSize(e.getTarget())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent e) { // todo this is temp, remove after riding fix (not soon)
        if (e.isMounting() && (getSize(e.getEntityMounting()) != 1.0F || getSize(e.getEntityBeingMounted()) != 1.0F)) {
            e.setCanceled(true);
        }
    }

    private static final SleepResult TOO_SMALL = EnumHelper.addStatus("TOO_SMALL");
    private static final SleepResult TOO_BIG = EnumHelper.addStatus("TOO_BIG");

    @SubscribeEvent
    public void onPlayerSleepInBed(PlayerSleepInBedEvent e) {
        EntityPlayer player = e.getEntityPlayer();
        float size = getSize(player);
        if (size < 1.0F) {
            e.setResult(TOO_SMALL);
            player.sendMessage(new TextComponentTranslation("chiseled_me.bed.too_small"));
        } else if (size > 1.0F) {
            e.setResult(TOO_BIG);
            player.sendMessage(new TextComponentTranslation("chiseled_me.bed.too_big"));
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent e) {
        float size = getSize(e.getEntity());
        if (size != 1.0F) {
            for (EntityItem item : e.getDrops()) {
                EntitySizeManager.setSize(item, size, false);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDrop(ItemTossEvent e) {
        float size = getSize(e.getPlayer());
        if (size != 1.0F) {
            EntitySizeManager.setSize(e.getEntityItem(), size, false);
        }
    }

    @SubscribeEvent
    public void onPlayerBreak(BlockEvent.HarvestDropsEvent e) {
        EntityPlayer player = e.getHarvester();
        float size;
        if (player != null && (size = getSize(player)) < 1.0) {
            for (ItemStack stack : e.getDrops()) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                }
                nbt.setFloat("chiseled_me:size", size);
                stack.setTagCompound(nbt);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerBreakSpeed(PlayerEvent.BreakSpeed e) {
        e.setNewSpeed(e.getNewSpeed() * getSize(e.getEntity()));
    }
}
