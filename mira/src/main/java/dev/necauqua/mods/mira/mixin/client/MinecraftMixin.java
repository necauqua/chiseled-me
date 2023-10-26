package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.size.MixinHelpers;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.necauqua.mods.mira.size.MixinHelpers.nearPatchRenderTarget;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public abstract MainWindow getWindow();

    // insert and early return into getMainRenderTarget
    @Inject(method = "getMainRenderTarget", at = @At("HEAD"), cancellable = true)
    void getMainRenderTarget(CallbackInfoReturnable<Framebuffer> cir) {
        if (MixinHelpers.renderingNearPatch) {
            cir.setReturnValue(MixinHelpers.getNearPatchRenderTarget(getWindow()));
        }
    }
}
