/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.item;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityEnderPearl.class)
public abstract class EntityEnderPearlMixin extends EntityMixin {

    @ModifyArg(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onImpact(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyConstant(method = "onImpact", constant = @Constant(doubleValue = 2.0))
    double onImpact(double constant) {
        return constant * $cm$size;
    }

    @Group(name = "onImpact", min = 1, max = 1)
    @ModifyArg(method = "onImpact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), expect = 0, require = 0)
    Entity onImpact(Entity entity) {
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }

    @SuppressWarnings("UnresolvedMixinReference") // method added by spigot and EnderPearlEntity patched to use it directly (to have SpawnReason.ENDER_PEARL)
    @Group(name = "onImpact", min = 1, max = 1)
    @ModifyArg(method = "onImpact", at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/World;addEntity(Lnet/minecraft/entity/Entity;Lorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;)Z"), expect = 0, require = 0)
    Entity onImpactSpigot(Entity entity) {
        ((ISized) entity).setSizeCM($cm$size);
        return entity;
    }
}
