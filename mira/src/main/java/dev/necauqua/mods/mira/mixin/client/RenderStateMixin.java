package dev.necauqua.mods.mira.mixin.client;

import net.minecraft.client.renderer.RenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static dev.necauqua.mods.mira.size.MixinHelpers.viewOffsetZLayeringScale;

@Mixin(RenderState.class)
public final class RenderStateMixin {

    @ModifyConstant(method = "lambda$static$24", constant = @Constant(floatValue = 0.99975586f))
    private static float viewOffsetZLayering(float constant) {
        return viewOffsetZLayeringScale;
    }
}
