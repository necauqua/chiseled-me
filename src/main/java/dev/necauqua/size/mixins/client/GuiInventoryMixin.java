package dev.necauqua.size.mixins.client;

import dev.necauqua.size.api.IRenderSized;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiInventory.class)
public final class GuiInventoryMixin {

    // cancel out the entity size when rendering it in GUIs
    @Redirect(method = "drawEntityOnScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V"))
    private static void scale(float x, float y, float z,
            int posX, int posY, int scale, float mouseX, float mouseY,
            EntityLivingBase entity) {
        double size = ((IRenderSized) entity).getSize();
        GlStateManager.scale(x / size, y / size, z / size);
    }
}
