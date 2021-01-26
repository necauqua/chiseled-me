package dev.necauqua.size.mixins.entity.item;

import dev.necauqua.size.api.ISized;
import dev.necauqua.size.mixins.entity.EntityMixin;
import dev.necauqua.size.EntitySizeInteractions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityFireworkRocket.class)
public abstract class EntityFireworkRocketMixin extends EntityMixin {

    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 0.3))
    double onUpdate(double constant) {
        return boostedEntity != null ?
                constant * ((ISized) boostedEntity).getSize() :
                constant * $size$it;
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    int[] onUpdate(int[] args) {
        return EntitySizeInteractions.appendSize(args,
                boostedEntity != null ?
                        ((ISized) boostedEntity).getSize() :
                        $size$it);
    }

    @Shadow
    private EntityLivingBase boostedEntity;
}
