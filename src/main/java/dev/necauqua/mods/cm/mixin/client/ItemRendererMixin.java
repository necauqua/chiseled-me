package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.IRenderSized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemRenderer.class)
public final class ItemRendererMixin {

    // vertical collision for rendering the being inside of a block overlay
    @ModifyConstant(method = "renderOverlays", constant = @Constant(floatValue = 0.1f))
    float renderOverlays(float constant) {
        return (float) (constant * ((IRenderSized) mc.player).getSizeCM());
    }

    @Shadow
    @Final
    private Minecraft mc;
}
