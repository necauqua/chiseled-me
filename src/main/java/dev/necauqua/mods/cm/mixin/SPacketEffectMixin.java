/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.size.IPreciseEffectPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(SPacketEffect.class)
public final class SPacketEffectMixin implements IPreciseEffectPacket {

    @Nullable
    private Vec3d $cm$pos;

    private double $cm$size = 1.0;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public Vec3d getCoordsCM() {
        return $cm$pos;
    }

    @Override
    public void populateCM(double size, Vec3d pos) {
        $cm$size = size;
        $cm$pos = pos;
    }

    @Inject(method = "readPacketData", at = @At(value = "TAIL"))
    void readPacketData(PacketBuffer buf, CallbackInfo ci) {
        if (buf.readableBytes() > 0) {
            $cm$size = buf.readDouble();
            $cm$pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }

    @Inject(method = "writePacketData", at = @At(value = "TAIL"))
    void writePacketData(PacketBuffer buf, CallbackInfo ci) {
        if ($cm$pos != null) {
            buf.writeDouble($cm$size);
            buf.writeDouble($cm$pos.x);
            buf.writeDouble($cm$pos.y);
            buf.writeDouble($cm$pos.z);
        }
    }
}
