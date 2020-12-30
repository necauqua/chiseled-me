package dev.necauqua.mods.cm.mixin.entity.item;

import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityItem.class)
public abstract class EntityItemMixin extends EntityMixin {

    // group range is [1;2] because either the first modifier is applied twice to vanilla constants
    // or the second applies once to the spigot localvar

    @Group(name = "searchForOtherItemsNearby", min = 1, max = 2)
    @ModifyConstant(method = "searchForOtherItemsNearby", constant = @Constant(doubleValue = 0.5), require = 0, expect = 0)
    double searchForOtherItemsNearby(double constant) {
        return constant * $cm$size;
    }

    @Group(name = "searchForOtherItemsNearby", min = 1, max = 2)
    @ModifyVariable(method = "searchForOtherItemsNearby", ordinal = 0, at = @At(value = "STORE", ordinal = 0), require = 0, expect = 0)
    double searchForOtherItemsNearbySpigot(double variable) {
        return variable * $cm$size;
    }
}
