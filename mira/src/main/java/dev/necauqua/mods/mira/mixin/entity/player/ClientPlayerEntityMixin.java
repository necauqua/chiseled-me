/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.entity.player;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    // scale the distance between sending movement packets
    @ModifyConstant(method = "sendPosition", constant = @Constant(doubleValue = 9.0E-4D))
    double onUpdateWalkingPlayer(double constant) {
        double size = ((ISized) this).getSizeCM();
        return size < 1.0 ? // only add precision, not lose it with big sizes
            constant * size * size : constant;
    }

    // region auto-jump shenanigans

    @ModifyConstant(method = "updateAutoJump", constant = @Constant(doubleValue = 0.5099999904632568))
    double updateAutoJump(double constant) {
        return constant * ((ISized) this).getSizeCM();
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
        return (float) (constant * ((ISized) this).getSizeCM());
    }

    // endregion
}
