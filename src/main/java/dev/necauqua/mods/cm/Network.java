/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm;

import dev.necauqua.mods.cm.ChiseledMe.Init;
import dev.necauqua.mods.cm.api.IRenderSized;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.util.function.Consumer;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;

@EventBusSubscriber(modid = MODID)
public final class Network {
    private static final FMLEventChannel channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MODID);

    private Network() {}

    @Init
    private static void init() {
        channel.register(Network.class);
    }

    @SubscribeEvent
    public static void onClientReceive(ClientCustomPacketEvent e) {
        PacketBuffer payload = new PacketBuffer(e.getPacket().payload());
        byte id = payload.readByte();
        switch (id) {
            case 0: {
                double size = payload.readDouble();
                int lerpTime = payload.readInt();
                SidedHandler.instance.scheduleClientMainLoopTask(() -> {
                    EntityPlayer player = SidedHandler.instance.getClientPlayer();
                    if (player != null) {
                        ((IRenderSized) player).setSizeCM(size, lerpTime);
                    }
                });
                break;
            }
            case 1: {
                int entityId = payload.readInt();
                double size = payload.readDouble();
                int lerpTime = payload.readInt();
                SidedHandler.instance.scheduleClientMainLoopTask(() -> {
                    World world = SidedHandler.instance.getClientWorld();
                    if (world == null) {
                        return;
                    }
                    Entity entity = world.getEntityByID(entityId);
                    if (entity != null) {
                        ((IRenderSized) entity).setSizeCM(size, lerpTime);
                    }
                });
                break;
            }
            default:
                invalidPacket(id, payload);
        }
    }

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

    public static void sync(Entity entity, double size, int lerpTime) {
        if (entity instanceof EntityPlayerMP) {
            channel.sendTo(packet(0, p -> {
                p.writeDouble(size);
                p.writeInt(lerpTime);
            }), (EntityPlayerMP) entity);
        }
        for (EntityPlayer entityPlayer : ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(entity)) {
            channel.sendTo(packet(1, p -> {
                p.writeInt(entity.getEntityId());
                p.writeDouble(size);
                p.writeInt(lerpTime);
            }), (EntityPlayerMP) entityPlayer);
        }
    }
}
