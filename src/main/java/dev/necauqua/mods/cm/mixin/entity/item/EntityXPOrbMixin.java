package dev.necauqua.mods.cm.mixin.entity.item;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityXPOrb.class)
public abstract class EntityXPOrbMixin extends EntityMixin {

    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 8.0))
    double onUpdate(double constant) {
        return constant * $cm$size;
    }

    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 64.0))
    double onUpdateSq(double constant) {
        return constant * $cm$size * $cm$size;
    }
}
