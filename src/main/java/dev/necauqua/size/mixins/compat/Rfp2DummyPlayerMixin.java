package dev.necauqua.size.mixins.compat;

import dev.necauqua.size.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.rejahtavi.rfp2.EntityPlayerDummy")
public abstract class Rfp2DummyPlayerMixin implements IRenderSized {

    // well we only need to forward getSize(float)
    // but forwarded everything for consistency idk

    @Override
    public double getSize(float partialTick) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null ?
                ((IRenderSized) player).getSize(partialTick) :
                1.0;
    }

    @Override
    public void setSize(double size, int lerpTime) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            ((IRenderSized) player).setSize(size, lerpTime);
        }
    }

    @Override
    public void setRawSize(double size) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            ((IRenderSized) player).setRawSize(size);
        }
    }

    @Override
    public int getResizingTicks() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null ?
                ((IRenderSized) player).getResizingTicks() :
                0;
    }

    @Override
    public double getSize() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null ?
                ((IRenderSized) player).getSize() :
                1.0;
    }
}
