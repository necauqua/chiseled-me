package dev.necauqua.size.mixins.entity.player;

import dev.necauqua.size.api.ISized;
import dev.necauqua.size.mixins.entity.EntityMixin;
import dev.necauqua.size.EntitySizeInteractions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityMixin {

    @ModifyVariable(method = "getEyeHeight", at = @At(value = "LOAD", ordinal = 1))
    float getEyeHeight(float local) {
        return (float) (local * $size$it);
    }

    // fixes collideEntityWithPlayer aabb expansion
    @ModifyConstant(method = "onLivingUpdate", constant = {
            @Constant(doubleValue = 0.5),
            @Constant(doubleValue = 1.0),
    })
    double onLivingUpdate(double constant) {
        return constant * $size$it;
    }

    @ModifyConstant(method = "attackTargetEntityWithCurrentItem", constant = {
            @Constant(doubleValue = 0.25),
            @Constant(doubleValue = 1.0),
            @Constant(doubleValue = 0.1, ordinal = 1),
            @Constant(doubleValue = 0.1, ordinal = 2),
    })
    double attackTargetEntityWithCurrentItem(double constant) {
        return constant * $size$it;
    }

    // dropped item vertical offset
    @ModifyConstant(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", constant = @Constant(doubleValue = 0.30000001192092896))
    double dropItem(double constant) {
        return constant * $size$it;
    }

    // damage heart particles
    @Redirect(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    void attackTargetEntityWithCurrentItem(WorldServer self, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int[] particleArguments, Entity target) {
        self.spawnParticle(particleType,
                xCoord, yCoord, zCoord,
                numberOfParticles,
                xOffset, yOffset, zOffset,
                particleSpeed,
                EntitySizeInteractions.appendSize(particleArguments, ((ISized) target).getSize()));
    }

    @ModifyArg(method = "spawnSweepParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    int[] spawnSweepParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $size$it);
    }

    @ModifyVariable(method = "spawnSweepParticles", at = @At("STORE"), ordinal = 0)
    double spawnSweepParticlesX(double local) {
        return local * $size$it;
    }

    @ModifyVariable(method = "spawnSweepParticles", at = @At("STORE"), ordinal = 1)
    double spawnSweepParticlesZ(double local) {
        return local * $size$it;
    }
}
