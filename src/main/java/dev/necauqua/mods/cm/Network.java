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

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;

public final class Network {
    private static FMLEventChannel channel;

    private Network() {}

    public static void init() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MODID);
        channel.register(new Network());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientReceive(ClientCustomPacketEvent e) {
        PacketBuffer payload = new PacketBuffer(e.getPacket().payload());
        byte id = payload.readByte();
        switch (id) {
            case 0:
                EntitySizeManager.setSizeClient(payload.readInt(), payload.readDouble(), payload.readBoolean());
                break;
            case 1:
                EntitySizeManager.setSizeClient(-1, payload.readDouble(), payload.readBoolean());
                break;
            default:
                invalidPacket(id, payload);
        }
    }

    public static void setSizeOnClient(EntityPlayerMP client, int entityId, double size, boolean interpolate) {
        channel.sendTo(packet(0, p -> {
            p.writeInt(entityId);
            p.writeDouble(size);
            p.writeBoolean(interpolate);
        }), client);
    }

    public static void setSizeOfClient(EntityPlayerMP client, double size, boolean interpolate) {
        channel.sendTo(packet(1, p -> {
            p.writeDouble(size);
            p.writeBoolean(interpolate);
        }), client);
    }

    @SuppressWarnings("SameParameterValue") // maybe in the future there will be some other packets
    private static FMLProxyPacket packet(int id, Consumer<PacketBuffer> data) {
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeByte(id);
        data.accept(payload);
        return new FMLProxyPacket(payload, MODID);
    }

    private static void invalidPacket(byte id, PacketBuffer payload) {
        StringBuilder out = new StringBuilder("Invalid packet received, its content was: ").append(id);
        int i = -1;
        while (payload.isReadable() && i++ < 16) {
            byte b = payload.readByte();
            String hex = Integer.toHexString(b);
            out.append(", ");
            if (hex.length() < 2) {
                out.append('0');
            }
            out.append(hex);
        }
        if (payload.isReadable()) {
            out.append("and ").append(payload.readableBytes()).append(" bytes more...");
        }
        Log.error(out);
    }
}
