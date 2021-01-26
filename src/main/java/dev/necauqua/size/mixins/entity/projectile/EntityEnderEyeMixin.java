package dev.necauqua.size.mixins.entity.projectile;

import dev.necauqua.size.api.ISized;
import dev.necauqua.size.mixins.entity.EntityMixin;
import dev.necauqua.size.EntitySizeInteractions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderEye;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import static org.objectweb.asm.Opcodes.GETFIELD;

@Mixin(EntityEnderEye.class)
public abstract class EntityEnderEyeMixin extends EntityMixin {

    @ModifyConstant(method = "onUpdate", constant = {
            @Constant(doubleValue = 0.25),
            @Constant(doubleValue = 0.5),
            @Constant(doubleValue = 0.6),
    })
    double onUpdate(double constant) {
        return constant * $size$it;
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/item/EntityEnderEye;motionX:D"))
    double onUpdateMotionX(EntityEnderEye self) {
        return self.motionX * $size$it;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/item/EntityEnderEye;motionY:D"))
    double onUpdateMotionY(EntityEnderEye self) {
        return self.motionY * $size$it;
    }

    @Redirect(method = "onUpdate", at = @At(value = "FIELD", opcode = GETFIELD, ordinal = 0, target = "Lnet/minecraft/entity/item/EntityEnderEye;motionZ:D"))
    double onUpdateMotionZ(EntityEnderEye self) {
        return self.motionZ * $size$it;
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    Entity onUpdateMotionX(Entity entity) {
        ((ISized) entity).setSize($size$it);
        return entity;
    }
}
