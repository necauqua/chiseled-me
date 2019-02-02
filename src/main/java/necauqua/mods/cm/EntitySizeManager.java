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

package necauqua.mods.cm;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

public final class EntitySizeManager {

    private EntitySizeManager() {}

    public static final float LOWER_LIMIT = 0.000244140625F; // = 1/16/16/16 = 1/4096
    public static final float UPPER_LIMIT = 16.0F;

    private static final Map<Integer, Float> spawnSetSizeQueue = Maps.newHashMap();

    public static EntitySizeData getData(Entity entity) {
        return entity.getCapability(EntitySizeData.CAPABILITY, null);
    }

    public static float getSize(Entity entity) {
        return getData(entity).currentSize;
    }

    public static void setSize(Entity entity, float size, boolean interp) {
        getData(entity).setSize(size, interp);
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

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent.Entity e) {
        e.addCapability(new ResourceLocation("chiseled_me", "size"), new EntitySizeData(e.getEntity()));
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
        if (entity.world.isRemote) {
            Float size = spawnSetSizeQueue.remove(entity.getEntityId());
            if (size != null) {
                setSize(entity, size, false);
            }
        }
    }

    @SubscribeEvent
    public void onDimChange(EntityTravelToDimensionEvent e) {
        EntitySizeManager.setSize(e.getEntity(), 1.0F, false);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent e) {
        float size = getData(e.player).setSize;
        if (size != 1.0F) {
            if (e.player instanceof EntityPlayerMP) {
                Network.sendEnqueueSetSizeToClient((EntityPlayerMP) e.player, e.player, size);
            }
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        Entity entity = e.getTarget();
        EntitySizeData data = getData(entity);
        if (entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) entity).getEntityItem();
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey("chiseled_me:size", 5)) {
                data.setSize(nbt.getFloat("chiseled_me:size"), false);
                nbt.removeTag("chiseled_me:size");
                stack.setTagCompound(nbt.hasNoTags() ?
                    null :
                    nbt);
            }
        }
        float size = data.setSize;
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

        private float prevSize = 1.0F, currentSize = 1.0F, setSize = 1.0F;
        private float prevRenderSize = 1.0F, renderSize = 1.0F;

        private float originalWidth = 1.0F, originalHeight = 1.0F;
        private boolean sizeWasSet = false;

        private int interpTime = 0, interp = 0;
        private boolean interping = false;

        private EntitySizeData(Entity entity) {
            this.entity = entity;
            isPlayer = entity instanceof EntityPlayer;
        }

        public void tick() {
            if (interping) {
                if (interp++ < interpTime) {
                    prevRenderSize = renderSize;
                    float s = prevSize + (setSize - prevSize) / interpTime * interp;
                    renderSize = s;
                    currentSize = s;
                    setBBoxSize(s);
                } else {
                    prevRenderSize = renderSize;
                    currentSize = setSize;
                    setBBoxSize(setSize);
                    interp = 0;
                    interping = false;
                }
            } else if (setSize != 1.0F && !isPlayer) { // players are handled by separate hook within mc code
                setBBoxSize(setSize);
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
            prevSize = size;
            currentSize = size;
            setSize = size;
            prevRenderSize = size;
            renderSize = size;
        }

        private void setSize(float size, boolean interp) {
            if (size >= LOWER_LIMIT && size <= UPPER_LIMIT) {
                Entity[] parts = entity.getParts();
                if (parts != null) {
                    for (Entity part : parts) {
                        getData(part).setSize(size, interp);
                    }
                }
                entity.dismountRidingEntity();
                entity.removePassengers();
                prevSize = setSize;
                setSize = size;
                if (interp) {
                    currentSize = prevSize;
                    float p = prevSize;
                    float s = size;
                    if (p < 1.0F) {
                        p = -1.0F / p;
                    }
                    if (s < 1.0F) {
                        s = -1.0F / s;
                    }
                    interpTime = MathHelper.log2(Math.abs((int) (s - p)) + 1) * 2;
                    interping = true;
                } else {
                    setBBoxSize(size);
                    setAllSizes(size);
                }
            }
        }

        public float getSize() {
            return currentSize;
        }

        public float getRenderSize(float partialTick) {
            return prevRenderSize + (renderSize - prevRenderSize) * partialTick;
        }

        @Override
        public NBTBase serializeNBT() {
            return new NBTTagFloat(setSize);
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
