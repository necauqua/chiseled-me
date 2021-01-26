package dev.necauqua.size.mixins.compat;

import dev.necauqua.size.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.therandomlabs.randompatches.hook.client.EntityRendererHook", remap = false)
public final class RandomPatchesFixMixin {

    private static boolean $size$wasResizing = false;

    // funny that this coerse+shadow combination allowed me to not compile-depend
    // on yet another 'fix-the-vanilla-mod-that-likely-has-no-maven' mod yey

    @Dynamic
    @Inject(method = "orientCamera", at = @At("HEAD"), cancellable = true)
    private static void orientCamera(float partialTicks, EntityRenderer renderer, @Coerce Object hook, CallbackInfo ci) {
        Entity entity = mc.getRenderViewEntity();
        assert entity != null;
        boolean isResizing = ((IRenderSized) entity).getResizingTicks() != 0;
        if (isResizing) {
            $size$wasResizing = true;
            ci.cancel();
        } else if ($size$wasResizing) {
            $size$wasResizing = false;
            ((RandomPatchesFixMixin) hook).lastEyeHeight = ((RandomPatchesFixMixin) hook).eyeHeight = entity.getEyeHeight();
        }
    }

    @Shadow
    @Final
    private static Minecraft mc;

    @Shadow
    private float lastEyeHeight;

    @Shadow
    private float eyeHeight;
}
