package dev.necauqua.mods.mira.mixin.client.sound;

import dev.necauqua.mods.mira.api.ISized;
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

    @Shadow
    @Final
    private AbstractMinecartEntity minecart;

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        ((ISized) this).setSizeCM(((ISized) minecart).getSizeCM());
    }
}
