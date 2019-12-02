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

import dev.necauqua.mods.cm.api.ISizedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static dev.necauqua.mods.cm.Network.setSizeOnClient;

public final class EntitySizeManager {

    public static final float LOWER_LIMIT = 0.000244140625f; // = 1/16/16/16 = 1/4096
    public static final float UPPER_LIMIT = 16.0f;

    private static final IntHashMap<SetSize> spawnSetSizeQueue = new IntHashMap<>();

    private EntitySizeManager() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new EntitySizeManager());
    }

    public static double getSize(Entity entity) {
        return ((ISizedEntity) entity).getEntitySize();
    }

    public static double getRenderSize(Entity entity, float partialTick) {
        return ((ISizedEntity) entity).getEntitySize(partialTick);
    }

    public static void setSize(Entity entity, double size, boolean interpolate) {
        if (size < LOWER_LIMIT || size > UPPER_LIMIT) {
            return;
        }
        entity.dismountRidingEntity();
        entity.removePassengers();

        Entity[] parts = entity.getParts();
        if (parts != null) {
            for (Entity part : parts) {
                setSize(part, size, interpolate);
            }
        }
        ((ISizedEntity) entity).setEntitySize(size, interpolate);
    }

    public static void setSizeAndSync(Entity entity, double size, boolean interpolate) {
        setSize(entity, size, interpolate);
        setSizeOnTrackingClients(entity, size, interpolate);
    }

    @SideOnly(Side.CLIENT)
    public static void setSizeClient(int entityId, double size, boolean interp) {
        Entity entity = ClientOnly.getEntityById(entityId);
        if (entity != null) {
            setSize(entity, size, interp);
        } else {
            spawnSetSizeQueue.addKey(entityId, new SetSize(size, interp));
        }
    }

    @SideOnly(Side.CLIENT)
    private static class ClientOnly {
        @Nullable
        private static Entity getEntityById(int id) {
            Minecraft mc = Minecraft.getMinecraft();
            if (id == -1) {
                return mc.player;
            }
            if (mc.world == null) {
                return null;
            }
            return mc.world.getEntityByID(id);
        }
    }

    private static int getEntityId(Entity entity) {
        return entity instanceof EntityPlayer ? -1 : entity.getEntityId();
    }

    public static void setSizeOnTrackingClients(Entity entity, double size, boolean interpolate) {
        if (entity.world.isRemote) {
            return;
        }
        int id = getEntityId(entity);
        if (id == -1) {
            Network.setSizeOnClient((EntityPlayerMP) entity, id, size, interpolate);
        }
        for (EntityPlayer entityPlayer : ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(entity)) {
            Network.setSizeOnClient((EntityPlayerMP) entityPlayer, id, size, interpolate);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
        if (entity.world.isRemote) {
            SetSize size = spawnSetSizeQueue.removeObject(getEntityId(entity));
            if (size != null) {
                setSize(entity, size.size, size.interp);
            }
        } else {
            double size = getSize(entity);
            if (size != 1.0) {
                setSizeOnTrackingClients(entity, size, false);
            }
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        Entity entity = e.getTarget();
        double size = getSize(entity);
        if (size != 1.0f) {
            setSizeOnClient((EntityPlayerMP) e.getEntityPlayer(), getEntityId(entity), size, false);
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone e) {
        if (!e.isWasDeath()) {
            setSizeAndSync(e.getOriginal(), getSize(e.getEntity()), false);
        }
    }

    private static class SetSize {
        private final double size;
        private final boolean interp;

        private SetSize(double size, boolean interp) {
            this.size = size;
            this.interp = interp;
        }
    }
}
