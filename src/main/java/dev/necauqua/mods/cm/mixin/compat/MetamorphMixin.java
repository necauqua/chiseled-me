/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.compat;

import dev.necauqua.mods.cm.api.IRenderSized;
import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.size.IEntityExtras;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.IFNE;

@Pseudo
@Mixin(targets = "mchorse.metamorph.api.morphs.AbstractMorph", remap = false)
public final class MetamorphMixin {

    //
    // "This is a total rip-off of EntityPlayer#setSize method"
    //
    // THEN WHY DAFUQ DONT YOU JUST CALL IT, WHY DO I KEEP NEEDING TO FIX OTHER MODS, UGH
    //
    // ok understandable, it is protected and access transformers are unhappy because
    // subclasses override it with the protected modifier and then Java is unhappy ugh
    //

    @Dynamic
    @ModifyVariable(method = "updateSizeDefault(Lnet/minecraft/entity/EntityLivingBase;FFF)V", ordinal = 0, at = @At(value = "JUMP", opcode = IFNE, ordinal = 1))
    private static float updateSizeDefaultWidth(float width, EntityLivingBase target) {
        ((IEntityExtras) target).setOriginalWidthCM(width);
        return (float) (width * ((ISized) target).getSizeCM());
    }

    @Dynamic
    @ModifyVariable(method = "updateSizeDefault(Lnet/minecraft/entity/EntityLivingBase;FFF)V", ordinal = 1, at = @At(value = "JUMP", opcode = IFNE, ordinal = 1))
    private static float updateSizeDefaultHeight(float height, EntityLivingBase target) {
        ((IEntityExtras) target).setOriginalHeightCM(height);
        return (float) (height * ((ISized) target).getSizeCM());
    }

    @Pseudo
    @Mixin(targets = "mchorse.metamorph.api.morphs.EntityMorph", remap = false)
    public static final class EntityMorphMixin {

        // to fix particles
        @Dynamic
        @Inject(method = "update", at = @At("TAIL"))
        void update(EntityLivingBase target, CallbackInfo ci) {
            ((IRenderSized) entity).setRawSizeCM(((ISized) target).getSizeCM());
        }

        @Shadow
        protected EntityLivingBase entity;
    }
}
