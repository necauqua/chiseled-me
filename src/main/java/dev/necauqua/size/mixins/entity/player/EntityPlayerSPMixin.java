package dev.necauqua.size.mixins.entity.player;

import dev.necauqua.size.mixins.entity.EntityMixin;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends EntityMixin {

    // push from blocks vertical aabb offset
    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.5))
    double onLivingUpdate(double constant) {
        return constant * $size$it;
    }

    // scale the distance between sending movement packets
    @ModifyConstant(method = "onUpdateWalkingPlayer", constant = @Constant(doubleValue = 0.0009))
    double onUpdateWalkingPlayer(double constant) {
        return constant * $size$it * $size$it;
    }

    @ModifyConstant(method = "updateAutoJump", constant = @Constant(doubleValue = 0.5099999904632568D))
    double updateAutoJump(double constant) {
        return constant * $size$it;
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
        return (float) (constant * $size$it);
    }

    // that getPosition fix
    @ModifyConstant(method = "getPosition", constant = @Constant(doubleValue = 0.5))
    double getPosition(double constant) {
        return constant * $size$it;
    }
}
