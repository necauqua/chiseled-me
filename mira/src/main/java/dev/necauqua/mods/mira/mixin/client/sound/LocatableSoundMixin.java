package dev.necauqua.mods.mira.mixin.client.sound;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.audio.ISound.AttenuationType.NONE;

@Mixin(LocatableSound.class)
public abstract class LocatableSoundMixin implements ISized {

    @Shadow
    protected double y;
    @Shadow
    protected double z;
    @Shadow
    protected double x;
    @Shadow
    protected ISound.AttenuationType attenuation;
    private double $cm$size = 1.0;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    // if attenuation is linear, we pretend that the sound is further away than it actually is
    // for some reason, I am doing this instead of just scaling the volume,
    // because y'now, attenuation is LINEAR

    @Override
    public void setSizeCM(double size) {
        $cm$size = size;
    }

    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    void getX(CallbackInfoReturnable<Double> cir) {
        if (attenuation == NONE || $cm$size == 1.0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Entity listener = mc.getCameraEntity();
        if (listener == null) {
            return;
        }
        double listenerX = MathHelper.lerp(mc.getFrameTime(), listener.xo, listener.getX());
        cir.setReturnValue(listenerX + (x - listenerX) / $cm$size);
    }

    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    void getY(CallbackInfoReturnable<Double> cir) {
        if (attenuation == NONE || $cm$size == 1.0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Entity listener = mc.getCameraEntity();
        if (listener == null) {
            return;
        }
        double listenerY = MathHelper.lerp(mc.getFrameTime(), listener.yo, listener.getY());
        cir.setReturnValue(listenerY + (y - listenerY) / $cm$size);
    }

    @Inject(method = "getZ", at = @At("HEAD"), cancellable = true)
    void getZ(CallbackInfoReturnable<Double> cir) {
        if (attenuation == NONE || $cm$size == 1.0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Entity listener = mc.getCameraEntity();
        if (listener == null) {
            return;
        }
        double listenerZ = MathHelper.lerp(mc.getFrameTime(), listener.zo, listener.getZ());
        cir.setReturnValue(listenerZ + (z - listenerZ) / $cm$size);
    }
}
