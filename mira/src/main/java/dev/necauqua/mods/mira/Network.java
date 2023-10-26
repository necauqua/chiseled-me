/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira;

import dev.necauqua.mods.mira.api.IRenderSized;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.JarVersionLookupHandler;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkEvent.ServerCustomPayloadEvent;
import net.minecraftforge.fml.network.NetworkRegistry;

import static dev.necauqua.mods.mira.Mira.MODID;
import static dev.necauqua.mods.mira.Mira.ns;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class Network {

    private static final ResourceLocation CHANNEL = ns("channel");

    @SubscribeEvent
    public static void on(FMLCommonSetupEvent e) {
        String version = JarVersionLookupHandler.getImplementationVersion(Mira.class).orElse("DEBUG");
        NetworkRegistry.ChannelBuilder
            .named(CHANNEL)
            .clientAcceptedVersions(version::equals)
            .serverAcceptedVersions(version::equals)
            .networkProtocolVersion(() -> version)
            .eventNetworkChannel()
            .registerObject(Handlers.class);
    }

    public static void sync(Entity entity, double size, int animationTicks) {
        if (entity.level instanceof ServerWorld) {
            PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
            payload.writeInt(entity.getId());
            payload.writeInt(animationTicks);
            payload.writeDouble(size);
            SCustomPayloadPlayPacket packet = new SCustomPayloadPlayPacket(CHANNEL, payload);
            ((ServerWorld) entity.level).getChunkSource().broadcastAndSend(entity, packet);
        }
    }

    private static final class Handlers {

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientReceive(ServerCustomPayloadEvent e) {
            PacketBuffer payload = e.getPayload();

            int entityId = payload.readInt();
            int animationTicks = payload.readInt();
            double size = payload.readDouble();

            Context ctx = e.getSource().get();
            ctx.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                World world = mc.level;
                if (world == null) {
                    return;
                }
                Entity entity = world.getEntity(entityId);
                if (entity != null) {
                    ((IRenderSized) entity).setSizeCM(size, animationTicks);
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
