/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.compat;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.client.RenderMixin;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Pseudo
@Mixin(targets = "noppes.npcs.entity.EntityNPCInterface", remap = false)
public abstract class CustomNpcFixMixin extends EntityMixin {

    @Dynamic
    @ModifyConstant(method = "onCollide", constant = @Constant(doubleValue = 1.0))
    double onCollide(double constant) {
        return constant * $cm$size;
    }

    @Group(name = "manualRemapAgainOmg", min = 1, max = 1)
    @Dynamic
    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 1.0, ordinal = 0), expect = 0, require = 0)
    double onUpdate(double constant) {
        return constant * $cm$size - 1.0; // yeah fixing other mods, as always
    }

    // that dumb superclass thing from @Pseudo documentation does not work
    // (not with EntityMixin nor just with plain Entity,
    // and I know about remap=false in mixin annotation, tried without it ofc)
    @Group(name = "manualRemapAgainOmg", min = 1, max = 1)
    @Dynamic
    @ModifyConstant(method = "func_70071_h_", constant = @Constant(doubleValue = 1.0, ordinal = 0), expect = 0, require = 0)
    double onUpdateObf(double constant) {
        return constant * $cm$size - 1.0;
    }

    @Pseudo
    @Mixin(targets = "noppes.npcs.entity.EntityCustomNpc", remap = false)
    private static abstract class EntityCustomNpcHitboxFix extends EntityMixin {

        @Group(name = "moreManualRemaps", min = 1, max = 1)
        @Dynamic
        @Inject(method = "updateHitbox", at = @At(value = "INVOKE", shift = AFTER, target = "Lnoppes/npcs/entity/EntityCustomNpc;setPosition(DDD)V"), expect = 0, require = 0)
        void updateHitbox(CallbackInfo ci) {
            setSize(width, height);
        }

        @Group(name = "moreManualRemaps", min = 1, max = 1)
        @Dynamic
        @Inject(method = "updateHitbox", at = @At(value = "INVOKE", shift = AFTER, target = "Lnoppes/npcs/entity/EntityCustomNpc;func_70107_b(DDD)V"), expect = 0, require = 0)
        void updateHitboxObf(CallbackInfo ci) {
            setSize(width, height);
        }
    }

    @Pseudo
    @Mixin(targets = {
            "noppes.npcs.entity.EntityNPCInterface",
            "noppes.npcs.entity.EntityNPCGolem",
            "noppes.npcs.entity.EntityNPCEnderman",
            "noppes.npcs.entity.EntityNpcDragon",
            "noppes.npcs.entity.EntityNpcSlime",
    }, remap = false)
    private static abstract class EntityNpcHitboxFixes extends EntityMixin {

        @Dynamic
        @Inject(method = "updateHitbox", at = @At("TAIL"))
        void updateHitbox(CallbackInfo ci) {
            setSize(width, height);
        }
    }

    @Pseudo
    @Mixin(targets = "noppes.npcs.client.renderer.RenderNPCInterface", remap = false)
    private static abstract class RenderFixes extends RenderMixin {

        @Dynamic
        @ModifyConstant(method = "renderName", constant = {
                @Constant(floatValue = 0.06F),
                @Constant(floatValue = 0.666667F),
        }, expect = 0, require = 0)
        float renderName(float constant) {
            return (float) (constant * $cm$sizeHack);
        }

        @Group(name = "crazyManualRemaps", min = 3, max = 3)
        @Dynamic
        @Redirect(method = "renderName", at = @At(value = "FIELD", target = "Lnoppes/npcs/entity/EntityNPCInterface;height:F"), expect = 0, require = 0)
        float renderNameHeight(@Coerce Entity self) {
            return (float) (self.height / ((ISized) self).getSizeCM());
        }

        @Group(name = "crazyManualRemaps", min = 3, max = 3)
        @Dynamic
        @Redirect(method = "renderName", at = @At(value = "FIELD", target = "Lnoppes/npcs/entity/EntityNPCInterface;field_70131_O:F"), expect = 0, require = 0)
        float renderNameHeightObf(@Coerce Entity self) {
            return (float) (self.height / ((ISized) self).getSizeCM());
        }

        @Group(name = "crazyManualRemaps", min = 3, max = 3)
        @Dynamic
        @Redirect(method = "doRenderShadowAndFire", at = @At(value = "FIELD", target = "Lnoppes/npcs/entity/EntityNPCInterface;width:F"), expect = 0, require = 0)
        float doRenderShadowAndFire(@Coerce Entity self) {
            return (float) (self.width / ((ISized) self).getSizeCM());
        }

        @Group(name = "crazyManualRemaps", min = 3, max = 3)
        @Dynamic
        @Redirect(method = "func_76979_b", at = @At(value = "FIELD", target = "Lnoppes/npcs/entity/EntityNPCInterface;field_70130_N:F"), expect = 0, require = 0)
        float doRenderShadowAndFireObf(@Coerce Entity self) {
            return (float) (self.width / ((ISized) self).getSizeCM());
        }
    }

    @Pseudo
    @Mixin(targets = "noppes.npcs.client.gui.util.GuiNPCInterface", remap = false)
    private static abstract class GuiNPCInterfaceMixin {

        @Group(name = "moarRemaps", min = 1, max = 1)
        @Dynamic
        @Redirect(method = "drawNpc(Lnet/minecraft/entity/EntityLivingBase;IIFI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V"), expect = 0, require = 0)
        void drawNpc(float x, float y, float z, EntityLivingBase entity) {
            double s = ((ISized) entity).getSizeCM();
            GlStateManager.scale(x / s, y / s, z / s);
        }

        @Group(name = "moarRemaps", min = 1, max = 1)
        @Dynamic
        @Redirect(method = "drawNpc(Lnet/minecraft/entity/EntityLivingBase;IIFI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;func_179152_a(FFF)V"), expect = 0, require = 0)
        void drawNpcObf(float x, float y, float z, EntityLivingBase entity) {
            double s = ((ISized) entity).getSizeCM();
            GlStateManager.scale(x / s, y / s, z / s);
        }
    }

    @Pseudo
    @Mixin(targets = "noppes.npcs.client.gui.util.GuiContainerNPCInterface", remap = false)
    private static abstract class GuiContainerNPCInterfaceMixin {

        private ISized $cm$sized; // can't shadow unavailable type apparently

        @Dynamic
        @Inject(method = "<init>", at = @At("TAIL"))
        void constructor(@Coerce ISized npc, Container cont, CallbackInfo ci) {
            $cm$sized = npc;
        }

        @Group(name = "moarRemaps2", min = 1, max = 1)
        @Dynamic
        @Redirect(method = "drawNpc", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V"), expect = 0, require = 0)
        void drawNpc(float x, float y, float z) {
            double s = $cm$sized.getSizeCM();
            GlStateManager.scale(x / s, y / s, z / s);
        }

        @Group(name = "moarRemaps2", min = 1, max = 1)
        @Dynamic
        @Redirect(method = "drawNpc", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;func_179152_a(FFF)V"), expect = 0, require = 0)
        void drawNpcObf(float x, float y, float z) {
            double s = $cm$sized.getSizeCM();
            GlStateManager.scale(x / s, y / s, z / s);
        }
    }
}
