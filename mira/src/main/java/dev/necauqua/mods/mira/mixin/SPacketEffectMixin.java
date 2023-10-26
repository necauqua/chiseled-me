/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin;

import dev.necauqua.mods.mira.extras.IPreciseEffectPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin({SPlaySoundEffectPacket.class, SPlaySoundEventPacket.class})
public final class SPacketEffectMixin implements IPreciseEffectPacket {

    @Nullable
    private Vector3d $cm$pos;

    private double $cm$size = 1.0;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public Vector3d getCoordsCM() {
        return $cm$pos;
    }

    @Override
    public void populateCM(double size, Vector3d pos) {
        $cm$size = size;
        $cm$pos = pos;
    }

    @Inject(method = "read", at = @At("TAIL"))
    void readPacketData(PacketBuffer buf, CallbackInfo ci) {
        if (buf.readableBytes() > 0) {
            $cm$size = buf.readDouble();
            $cm$pos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    void writePacketData(PacketBuffer buf, CallbackInfo ci) {
        if ($cm$pos != null) {
            buf.writeDouble($cm$size);
            buf.writeDouble($cm$pos.x);
            buf.writeDouble($cm$pos.y);
            buf.writeDouble($cm$pos.z);
        }
    }
}
