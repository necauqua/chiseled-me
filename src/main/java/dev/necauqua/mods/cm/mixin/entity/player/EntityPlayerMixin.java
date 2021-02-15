/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.player;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import static org.objectweb.asm.Opcodes.IFNE;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityMixin {

    @ModifyVariable(method = "getEyeHeight", at = @At(value = "LOAD", ordinal = 1))
    float getEyeHeight(float local) {
        return (float) (local * $cm$size);
    }

    @ModifyVariable(method = "updateSize", ordinal = 0, at = @At(value = "JUMP", opcode = IFNE, ordinal = 0))
    float updateSizeWidth(float f) {
        return (float) (f * $cm$size);
    }

    @ModifyVariable(method = "updateSize", ordinal = 1, at = @At(value = "JUMP", opcode = IFNE, ordinal = 0))
    float updateSizeHeight(float f1) {
        return (float) (f1 * $cm$size);
    }

    // and then undo it for the setSize, lol
    @Redirect(method = "updateSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSize(FF)V"))
    void updateSize(EntityPlayer entityPlayer, float width, float height) {
        setSize((float) (width / $cm$size), (float) (height / $cm$size));
    }

    // fixes collideEntityWithPlayer aabb expansion
    @ModifyConstant(method = "onLivingUpdate", constant = {
            @Constant(doubleValue = 0.5),
            @Constant(doubleValue = 1.0),
    })
    double onLivingUpdate(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "attackTargetEntityWithCurrentItem", constant = {
            @Constant(doubleValue = 0.25),
            @Constant(doubleValue = 1.0),
            @Constant(doubleValue = 0.1, ordinal = 1),
            @Constant(doubleValue = 0.1, ordinal = 2),
    })
    double attackTargetEntityWithCurrentItem(double constant) {
        return constant * $cm$size;
    }

    // dropped item vertical offset
    @ModifyConstant(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", constant = @Constant(doubleValue = 0.30000001192092896))
    double dropItem(double constant) {
        return constant * $cm$size;
    }

    // damage heart particles
    @Redirect(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    void attackTargetEntityWithCurrentItem(WorldServer self, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int[] particleArguments, Entity target) {
        self.spawnParticle(particleType,
                xCoord, yCoord, zCoord,
                numberOfParticles,
                xOffset, yOffset, zOffset,
                particleSpeed,
                EntitySizeInteractions.appendSize(particleArguments, ((ISized) target).getSizeCM()));
    }

    @ModifyArg(method = "spawnSweepParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    int[] spawnSweepParticles(int[] args) {
        return EntitySizeInteractions.appendSize(args, $cm$size);
    }

    @ModifyVariable(method = "spawnSweepParticles", at = @At("STORE"), ordinal = 0)
    double spawnSweepParticlesX(double local) {
        return local * $cm$size;
    }

    @ModifyVariable(method = "spawnSweepParticles", at = @At("STORE"), ordinal = 1)
    double spawnSweepParticlesZ(double local) {
        return local * $cm$size;
    }
}
