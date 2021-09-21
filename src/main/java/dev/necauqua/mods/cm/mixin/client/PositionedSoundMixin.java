package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.audio.ISound.AttenuationType.NONE;

@Mixin(PositionedSound.class)
public final class PositionedSoundMixin implements ISized {

    private double $cm$size = 1.0;

    @Override
    public double getSizeCM() {
        return $cm$size;
    }

    @Override
    public void setSizeCM(double size) {
        $cm$size = size;
    }

    @Inject(method = "getXPosF", at = @At("RETURN"), cancellable = true)
    void getXPosF(CallbackInfoReturnable<Float> cir) {
        if (attenuationType == NONE || $cm$size == 1.0) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Entity listener = mc.getRenderViewEntity();
        if (listener == null) {
            return;
        }
        double listenerX = listener.prevPosX + (listener.posX - listener.prevPosX) * mc.getRenderPartialTicks();
        cir.setReturnValue((float) (listenerX + (cir.getReturnValueF() - listenerX) / $cm$size));
    }

    @Inject(method = "getYPosF", at = @At("RETURN"), cancellable = true)
    void getYPosF(CallbackInfoReturnable<Float> cir) {
        if (attenuationType == NONE || $cm$size == 1.0) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Entity listener = mc.getRenderViewEntity();
        if (listener == null) {
            return;
        }
        double listenerY = listener.prevPosY + (listener.posY - listener.prevPosY) * mc.getRenderPartialTicks() + listener.getEyeHeight();
        cir.setReturnValue((float) (listenerY + (cir.getReturnValueF() - listenerY) / $cm$size));
    }

    @Inject(method = "getZPosF", at = @At("RETURN"), cancellable = true)
    void getZPosF(CallbackInfoReturnable<Float> cir) {
        if (attenuationType == NONE || $cm$size == 1.0) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Entity listener = mc.getRenderViewEntity();
        if (listener == null) {
            return;
        }
        double listenerZ = listener.prevPosZ + (listener.posZ - listener.prevPosZ) * mc.getRenderPartialTicks();
        cir.setReturnValue((float) (listenerZ + (cir.getReturnValueF() - listenerZ) / $cm$size));
    }

    @Shadow
    protected ISound.AttenuationType attenuationType;
}
