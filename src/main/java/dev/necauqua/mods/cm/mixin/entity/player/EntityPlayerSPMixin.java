/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity.player;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends EntityMixin {

    // push from blocks vertical aabb offset
    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.5))
    double onLivingUpdate(double constant) {
        return constant * $cm$size;
    }

    // scale the distance between sending movement packets
    @ModifyConstant(method = "onUpdateWalkingPlayer", constant = @Constant(doubleValue = 9.0E-4D))
    double onUpdateWalkingPlayer(double constant) {
        return $cm$size < 1.0 ? // only add precision, not lose it with big sizes
                constant * $cm$size * $cm$size :
                constant;
    }

    @ModifyConstant(method = "updateAutoJump", constant = @Constant(doubleValue = 0.5099999904632568D))
    double updateAutoJump(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "updateAutoJump", constant = @Constant(intValue = 1, ordinal = 1))
    int updateAutoJump(int constant) {
        return 0;
    }

    @ModifyConstant(method = "updateAutoJump", constant = {
            @Constant(floatValue = 0.001f),
            @Constant(floatValue = -0.15f, ordinal = 0),
            @Constant(floatValue = 0.75f, ordinal = 0),
            @Constant(floatValue = 1.2f, ordinal = 0),
            @Constant(floatValue = 7.0f, ordinal = 1),
            @Constant(floatValue = 0.5f, ordinal = 1),
    })
    float updateAutoJump(float constant) {
        return (float) (constant * $cm$size);
    }

    // that getPosition fix
    @ModifyConstant(method = "getPosition", constant = @Constant(doubleValue = 0.5))
    double getPosition(double constant) {
        return constant * $cm$size;
    }
}
