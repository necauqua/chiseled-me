package dev.necauqua.mods.cm2;

import dev.necauqua.mods.mira.api.IRenderSized;
import io.netty.buffer.Unpooled;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.JarVersionLookupHandler;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry.ChannelBuilder;

import static dev.necauqua.mods.cm2.ChiseledMe2.MODID;
import static dev.necauqua.mods.cm2.ChiseledMe2.ns;
import static dev.necauqua.mods.mira.api.IResizingProgress.log2LerpTime;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class Network {

    private static final ResourceLocation CHANNEL = ns("channel");

    @SubscribeEvent
    public static void on(FMLCommonSetupEvent e) {
        String version = JarVersionLookupHandler.getImplementationVersion(ChiseledMe2.class).orElse("DEBUG");
        ChannelBuilder.named(CHANNEL)
            .clientAcceptedVersions(version::equals)
            .serverAcceptedVersions(version::equals)
            .networkProtocolVersion(() -> version)
            .eventNetworkChannel()
            .registerObject(Handlers.class);
    }

    public enum GuiEvent {
        MUL,
        DIV,
        TEST1,
        TEST2,
        TEST3,
        TEST4,
    }

    @OnlyIn(Dist.CLIENT)
    public static void guiEvent(ClientPlayerEntity player, GuiEvent event) {
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer());
        payload.writeByte(event.ordinal());
        player.connection.send(new CCustomPayloadPacket(CHANNEL, payload));
    }

    private static final class Handlers {

        private Handlers() {
        }

        @SubscribeEvent
        public static void onServerReceive(NetworkEvent.ClientCustomPayloadEvent e) {
            PacketBuffer payload = e.getPayload();
            int ordinal = payload.readByte();
            if (ordinal < 0 || ordinal >= GuiEvent.values().length) {
                System.out.println("bad packet"); // todo maybe make a logger lol
                return;
            }
            GuiEvent event = GuiEvent.values()[ordinal];
            NetworkEvent.Context ctx = e.getSource().get();
            if (ctx.getSender() == null) {
                return;
            }
            ctx.enqueueWork(() -> {
                IRenderSized sized = (IRenderSized) ctx.getSender();
                double currentSize = sized.getSizeCM();
                double size = currentSize;
                switch (event) {
                    case MUL:
                        size = currentSize * 2;
                        break;
                    case DIV:
                        size = currentSize / 2;
                        break;
                }
                sized.setSizeCM(size);
                if (size != currentSize) {
                    sized.setSizeCM(size, log2LerpTime(currentSize, size));
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}

