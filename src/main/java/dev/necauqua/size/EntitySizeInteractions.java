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

package dev.necauqua.size;

import dev.necauqua.size.api.IRenderSized;
import dev.necauqua.size.api.ISized;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
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
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

import static dev.necauqua.size.Size.*;
import static dev.necauqua.size.SizeofCommand.LOWER_LIMIT;

@EventBusSubscriber(modid = Size.ID)
public final class EntitySizeInteractions {

    private static final String NBT_KEY_SIZE = Size.ID + ":size";

    private EntitySizeInteractions() {}

    @SubscribeEvent
    public static void on(EntityInteractSpecific e) {
        if (((ISized) e.getEntity()).getSize() != ((ISized) e.getTarget()).getSize()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void on(EntityMountEvent e) {
        double mountingSize = ((ISized) e.getEntityMounting()).getSize();
        double mountedSize = e.getEntityBeingMounted() != null ? ((ISized) e.getEntityBeingMounted()).getSize() : 1.0;
        if (e.isMounting() && (mountingSize != 1.0 || mountedSize != 1.0)) {
            e.setCanceled(true);
        }
    }

    private static final SleepResult TOO_BIG = EnumHelper.addStatus("TOO_BIG");

    @SubscribeEvent
    public static void on(PlayerSleepInBedEvent e) {
        EntityPlayer player = e.getEntityPlayer();
        if (((ISized) player).getSize() > 1.0) {
            e.setResult(TOO_BIG);
            player.sendMessage(new TextComponentTranslation("size.too_big_to_sleep"));
        }
    }

    @SubscribeEvent
    public static void on(LivingDropsEvent e) {
        double size = ((ISized) e.getEntity()).getSize();
        if (size == 1.0) {
            return;
        }
        for (EntityItem item : e.getDrops()) {
            ((ISized) item).setSize(size);
        }
    }

    @SubscribeEvent
    public static void on(ItemTossEvent e) {
        double size = ((ISized) e.getPlayer()).getSize();
        if (size != 1.0) {
            ((ISized) e.getEntity()).setSize(size);
        }
    }

    @SubscribeEvent
    public static void on(BlockEvent.HarvestDropsEvent e) {
        EntityPlayer player = e.getHarvester();
        if (player == null) {
            return;
        }
        double size = ((ISized) player).getSize();
        if (size == 1.0) {
            return;
        }
        for (ItemStack stack : e.getDrops()) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                nbt = new NBTTagCompound();
            }
            nbt.setDouble(NBT_KEY_SIZE, size);
            stack.setTagCompound(nbt);
        }
    }

    private static void applyDefaultSize(Entity entity, String entitySizeRule) {
        try {
            double size = MathHelper.clamp(Double.parseDouble(entity.world.getGameRules().getString(entitySizeRule)), LOWER_LIMIT, Config.maxSize);
            if (size != 1.0 && ((ISized) entity).getSize() == 1.0) {
                ((ISized) entity).setSize(size);
            }
        } catch (NumberFormatException ignored) {}
    }

    @SubscribeEvent
    public static void on(PlayerEvent.Clone e) {
        if (e.isWasDeath() && e.getEntityPlayer().world.getGameRules().getBoolean(KEEP_SIZE_RULE)) {
            ((ISized) e.getEntityPlayer()).setSize(((ISized) e.getOriginal()).getSize());
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
                ((ISized) entity).setSize(nbt.getDouble(NBT_KEY_SIZE));
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
        double size = ((ISized) thrower).getSize();
        if (size != 1.0) {
            ((ISized) entity).setSize(size);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void on(PlayerEvent.BreakSpeed e) {
        e.setNewSpeed((float) (e.getNewSpeed() * ((ISized) e.getEntity()).getSize()));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void on(RenderGameOverlayEvent.Text e) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        double size = ((ISized) player).getSize();
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
        double size = ((ISized) e.getParentA()).getSize();
        Entity child = e.getChild();
        if (child != null && size != 1.0) {
            ((ISized) child).setSize(size);
        }
    }

    public static void onEntitySetSize(Entity entity, double fromSize, double toSize) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isPlayerSleeping()) {
            ((EntityPlayer) entity).wakeUpPlayer(true, !entity.world.isRemote, true);
        }
        if (entity instanceof EntityPlayerMP) {
            SizeAdvancementTrigger.trigger((EntityPlayerMP) entity, fromSize, toSize);
        }
    }

    public static double getAverageSize(Object a, Object b) {
        double sa = ((ISized) a).getSize();
        double sb = ((ISized) b).getSize();
        return sa == sb ? sa : Math.sqrt(sa * sb);
    }

    @SideOnly(Side.CLIENT)
    public static double getViewerSize() {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        return viewer != null ?
                ((ISized) viewer).getSize() :
                1.0;
    }

    @SideOnly(Side.CLIENT)
    public static double getViewerSize(float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        return viewer != null ?
                ((IRenderSized) viewer).getSize(partialTicks) :
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
