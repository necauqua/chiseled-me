package dev.necauqua.mods.cm2.recipes;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface ITriggeringRecipe {

    @Nullable
    ResourceLocation getTrigger();
}
