package dev.necauqua.size.mixins;

import dev.necauqua.size.IPreciseEffectPacket;
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
    private Vec3d $size$pos;

    private double $size$it = 1.0;

    @Override
    public double $size$getSize() {
        return $size$it;
    }

    @Override
    public Vec3d $size$getCoords() {
        return $size$pos;
    }

    @Override
    public void $size$populate(double size, Vec3d pos) {
        $size$it = size;
        $size$pos = pos;
    }

    @Inject(method = "readPacketData", at = @At(value = "TAIL"))
    void readPacketData(PacketBuffer buf, CallbackInfo ci) {
        if (buf.readableBytes() > 0) {
            $size$it = buf.readDouble();
            $size$pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }

    @Inject(method = "writePacketData", at = @At(value = "TAIL"))
    void writePacketData(PacketBuffer buf, CallbackInfo ci) {
        if ($size$pos != null) {
            buf.writeDouble($size$it);
            buf.writeDouble($size$pos.x);
            buf.writeDouble($size$pos.y);
            buf.writeDouble($size$pos.z);
        }
    }
}
