package dev.necauqua.mods.mira.mixin.client.sound;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.client.audio.BeeSound;
import net.minecraft.entity.passive.BeeEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeeSound.class)
public abstract class BeeSoundMixin {

    @Shadow
    @Final
    protected BeeEntity bee;

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        ((ISized) this).setSizeCM(((ISized) bee).getSizeCM());
    }
}
