package dev.necauqua.mods.theta.mixin.client.sound;

import dev.necauqua.mods.theta.api.ISized;
import net.minecraft.client.audio.MinecartTickableSound;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartTickableSound.class)
public abstract class MinecartTickableSoundMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        ((ISized) this).setSizeCM(((ISized) minecart).getSizeCM());
    }

    @Shadow
    @Final
    private AbstractMinecartEntity minecart;
}
