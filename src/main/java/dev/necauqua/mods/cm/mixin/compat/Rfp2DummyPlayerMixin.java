/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.compat;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.rejahtavi.rfp2.EntityPlayerDummy")
public abstract class Rfp2DummyPlayerMixin implements IRenderSized {

    // well we only need to forward getSizeCM(float)
    // but forwarded everything for consistency idk

    @Override
    public double getSizeCM(float partialTick) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null ?
                ((IRenderSized) player).getSizeCM(partialTick) :
                1.0;
    }

    @Override
    public void setSizeCM(double size, int lerpTime) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            ((IRenderSized) player).setSizeCM(size, lerpTime);
        }
    }

    @Override
    public void setRawSizeCM(double size) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            ((IRenderSized) player).setRawSizeCM(size);
        }
    }

    @Override
    public boolean isResizingCM() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null && ((IRenderSized) player).isResizingCM();
    }

    @Override
    public double getSizeCM() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null ?
                ((IRenderSized) player).getSizeCM() :
                1.0;
    }

    @Override
    public void updateCM() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            ((IRenderSized) player).updateCM();
        }
    }
}
