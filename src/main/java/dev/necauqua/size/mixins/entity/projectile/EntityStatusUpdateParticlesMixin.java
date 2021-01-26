package dev.necauqua.size.mixins.entity.projectile;

import dev.necauqua.size.mixins.entity.EntityMixin;
import dev.necauqua.size.EntitySizeInteractions;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityTippedArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({EntitySnowball.class, EntityTippedArrow.class})
public abstract class EntityStatusUpdateParticlesMixin extends EntityMixin {

    @ModifyArg(method = "handleStatusUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] handleStatusUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }
}
