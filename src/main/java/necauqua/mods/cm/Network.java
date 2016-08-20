/*
 * Copyright (c) 2016 Anton Bulakh
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

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public final class Network {

    private Network() {}

    private static FMLEventChannel chan;

    public static void init() {
        chan = NetworkRegistry.INSTANCE.newEventDrivenChannel("chiseled_me");
        chan.register(new Network());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientReceive(ClientCustomPacketEvent e) {
        PacketBuffer payload = new PacketBuffer(e.getPacket().payload());
        switch(payload.readByte()) {
            case 0: {
                World clientWorld = Minecraft.getMinecraft().theWorld;
                if(clientWorld != null) {
                    int id = payload.readInt();
                    Entity entity = clientWorld.getEntityByID(id);
                    if(entity != null) {
                        EntitySizeManager.setSize(entity, payload.readFloat(), payload.readBoolean());
                    }else {
                        Log.warn("Client entity with id " + id + " is null! This mean you're desynced somewhere =/");
                    }
                }else {
                    Log.warn("Somehow client world does not yet exist, this should never happen!");
                }
                break;
            }
            case 1: {
                EntitySizeManager.enqueueSetSize(payload.readInt(), payload.readFloat());
                break;
            }
        }
    }

    private static FMLProxyPacket packet(int id, Consumer<PacketBuffer> data) {
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeByte(id);
        data.accept(payload);
        return new FMLProxyPacket(payload, "chiseled_me");
    }

    public static void sendSetSizeToClients(Entity entity, float size, boolean interp) {
        chan.sendToDimension(packet(0, p -> {
            p.writeInt(entity.getEntityId());
            p.writeFloat(size);
            p.writeBoolean(interp);
        }), entity.dimension);
    }

    public static void sendEnqueueSetSizeToClient(EntityPlayerMP client, Entity entity, float size) {
        chan.sendTo(packet(1, p -> {
            p.writeInt(entity.getEntityId());
            p.writeFloat(size);
        }), client);
    }
}