package dev.necauqua.mods.mira.mixin.entity.projectile;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EggEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EggEntity.class)
public abstract class EggEntityMixin {

    @ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addFreshEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity onHit(Entity entity) {
        ((ISized) entity).setSizeCM(((ISized) this).getSizeCM());
        return entity;
    }
}
