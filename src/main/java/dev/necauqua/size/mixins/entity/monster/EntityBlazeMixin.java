package dev.necauqua.size.mixins.entity.monster;

import dev.necauqua.size.mixins.entity.EntityMixin;
import dev.necauqua.size.EntitySizeInteractions;
import net.minecraft.entity.monster.EntityBlaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityBlaze.class)
public abstract class EntityBlazeMixin extends EntityMixin {

    @ModifyArg(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onLivingUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }
}
