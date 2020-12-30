package dev.necauqua.mods.cm.mixin.entity.player;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityOtherPlayerMP.class)
public abstract class EntityOtherPlayerMPMixin extends EntityMixin {

    // another limb swing animation, meh
    @ModifyConstant(method = "onUpdate", constant = @Constant(floatValue = 4.0f))
    float onUpdate(float constant) {
        return (float) (constant / $cm$size);
    }

    // that getPosition fix
    @ModifyConstant(method = "getPosition", constant = @Constant(doubleValue = 0.5))
    double getPosition(double constant) {
        return constant * $cm$size;
    }
}
