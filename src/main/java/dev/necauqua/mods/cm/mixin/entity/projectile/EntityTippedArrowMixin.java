/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.projectile;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(EntityTippedArrow.class)
public abstract class EntityTippedArrowMixin extends EntityMixin {

    // FIXING VANILLA BUGS OMFG
    @Redirect(method = "refreshColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtils;getPotionColorFromEffectList(Ljava/util/Collection;)I"))
    int findEntityOnPath(Collection<PotionEffect> effects) {
        return effects.isEmpty() ? -1 : PotionUtils.getPotionColorFromEffectList(effects);
    }

    @ModifyArg(method = "spawnPotionParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] spawnPotionParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }
}
