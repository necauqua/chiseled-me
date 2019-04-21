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

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static dev.necauqua.mods.cm.EntitySizeManager.EntitySizeData.CAPABILITY;
import static java.lang.Math.*;

public final class EntitySizeManager {

    public static final String NBT_KEY_SIZE = MODID + ":size";

    private static final double TWO_OVER_LOG_TWO = 2.0 / log(2);

    private EntitySizeManager() {}

    public static final float LOWER_LIMIT = 0.000244140625F; // = 1/16/16/16 = 1/4096
    public static final float UPPER_LIMIT = 16.0F;

    private static final Map<Integer, Float> spawnSetSizeQueue = Maps.newHashMap();

    public static float getSize(Entity entity) {
        return entity.getCapability(CAPABILITY, null).interpSize;
    }

    public static float getRenderSize(Entity entity, float partialTick) {
        return entity.getCapability(CAPABILITY, null).getRenderSize(partialTick);
    }

    public static void setSize(Entity entity, float size, boolean interp) {
        entity.getCapability(CAPABILITY, null).setSize(size, interp);
    }

    public static void updateSize(Entity entity) {
        entity.getCapability(CAPABILITY, null).updateSize();
    }

    @SideOnly(Side.CLIENT)
    public static void enqueueSetSize(int entityId, float size) {
        World clientWorld = Minecraft.getMinecraft().world;
        if (clientWorld != null) {
            Entity entity = clientWorld.getEntityByID(entityId);
            if (entity != null) {
                setSize(entity, size, false);
                return;
            }
        }
        spawnSetSizeQueue.put(entityId, size);
    }

    @SuppressWarnings("deprecation") // why?
    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent.Entity e) {
        e.addCapability(new ResourceLocation(MODID, "size"), new EntitySizeData(e.getEntity()));
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
        if (entity.world.isRemote) {
            Float size = spawnSetSizeQueue.remove(entity.getEntityId());
            if (size != null) {
                setSize(entity, size, false);
            }
        } else {
            float size = getSize(entity);
            if (size != 1.0F) {
                Network.sendSetSizeToClients(entity, size, false);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone e) {
        EntityPlayer player = e.getEntityPlayer();
        if (!e.isWasDeath() && player instanceof EntityPlayerMP) {
            float size = getSize(player);
            if (size != 1.0F) {
                Network.sendEnqueueSetSizeToClient((EntityPlayerMP) player, player, size);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent e) {
        float size = ((Entity) e.player).getCapability(CAPABILITY, null).nextSize;
        if (size != 1.0F) {
            if (e.player instanceof EntityPlayerMP) {
                Network.sendEnqueueSetSizeToClient((EntityPlayerMP) e.player, e.player, size);
            }
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        Entity entity = e.getTarget();
        EntitySizeData data = entity.getCapability(CAPABILITY, null);
        if (entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) entity).getEntityItem();
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey(NBT_KEY_SIZE, 5)) {
                data.setSize(nbt.getFloat(NBT_KEY_SIZE), false);
                nbt.removeTag(NBT_KEY_SIZE);
                if (nbt.hasNoTags()) {
                    //noinspection ConstantConditions - tag compound is nullable, lol
                    stack.setTagCompound(null);
                }
            }
        }
        float size = data.nextSize;
        if (size != 1.0F) {
            EntityPlayer player = e.getEntityPlayer();
            if (player instanceof EntityPlayerMP) {
                Network.sendEnqueueSetSizeToClient((EntityPlayerMP) player, entity, size);
            }
        }
    }

    public static final class EntitySizeData implements ICapabilitySerializable<NBTBase> {

        @CapabilityInject(EntitySizeData.class)
        public static Capability<EntitySizeData> CAPABILITY = null;

        private final Entity entity;
        private final boolean isPlayer;

        private float prevSize = 1.0F, nextSize = 1.0F;
        private float prevInterpSize = 1.0F, interpSize = 1.0F;

        private float originalWidth = 1.0F, originalHeight = 1.0F;
        private boolean sizeWasSet = false;

        private int interpInterval = 0, interpTicks = 0;

        private EntitySizeData(Entity entity) {
            this.entity = entity;
            isPlayer = entity instanceof EntityPlayer;
        }

        public void updateSize() {
            if (interpInterval != 0) {
                if (interpTicks++ < interpInterval) {
                    prevInterpSize = interpSize;
                    setBBoxSize(interpSize = prevSize + (nextSize - prevSize) / interpInterval * interpTicks);
                    return;
                }
                interpTicks = 0;
                prevInterpSize = interpSize;
                interpSize = nextSize;
                setBBoxSize(nextSize);
                interpInterval = 0;
            } else if (nextSize != 1.0F && !isPlayer) { // players are handled by separate hook within mc code
                setBBoxSize(nextSize);
            }
        }

        private void setBBoxSize(float size) {
            if (!sizeWasSet) { // can't do this in constructor because it's called at end of Entity constructor where size is still set to default
                originalWidth = entity.width;
                originalHeight = entity.height;
                sizeWasSet = true;
            }
            Vec3d pos = entity.getPositionVector();
            float w = originalWidth * size / 2.0F;
            float h = originalHeight * size;
            entity.width = w * 2.0F;
            entity.height = h;
            entity.setEntityBoundingBox(new AxisAlignedBB(pos.xCoord - w, pos.yCoord, pos.zCoord - w, pos.xCoord + w, pos.yCoord + h, pos.zCoord + w));
        }

        private void setAllSizes(float size) {
            interpTicks = 0;
            interpInterval = 0;
            prevSize = size;
            interpSize = size;
            nextSize = size;
            prevInterpSize = size;
        }

        private void setSize(float size, boolean interp) {
            if (size < LOWER_LIMIT || size > UPPER_LIMIT) {
                return;
            }
            Entity[] parts = entity.getParts();
            //noinspection ConstantConditions - parts ARE nullable, lol
            if (parts != null) {
                for (Entity part : parts) {
                    part.getCapability(CAPABILITY, null).setSize(size, interp);
                }
            }
            entity.dismountRidingEntity();
            entity.removePassengers();
            prevSize = interpSize;
            nextSize = size;
            if (interp) {
                interpTicks = 0;
                interpInterval = (int) (abs(log(prevSize) - log(nextSize)) * TWO_OVER_LOG_TWO);
                return;
            }
            setBBoxSize(size);
            setAllSizes(size);
        }

        public float getSize() {
            return interpSize;
        }

        public float getRenderSize(float partialTick) {
            return prevInterpSize + (interpSize - prevInterpSize) * partialTick;
        }

        @Override
        public NBTBase serializeNBT() {
            return new NBTTagFloat(nextSize);
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            if (nbt instanceof NBTPrimitive) {
                setAllSizes(((NBTPrimitive) nbt).getFloat());
            }
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY ?
                CAPABILITY.cast(this) :
                null;
        }
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(EntitySizeData.class, new Capability.IStorage<EntitySizeData>() {

            @Override
            public NBTBase writeNBT(Capability<EntitySizeData> capability, EntitySizeData instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<EntitySizeData> capability, EntitySizeData instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT(nbt);
            }
        }, EntitySizeData.class);
        MinecraftForge.EVENT_BUS.register(new EntitySizeManager());
    }
}
