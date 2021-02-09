/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.entity;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// every vanilla entity with hardcoded getEyeHeight, yey
@Mixin({
        EntityWitherSkeleton.class,
        EntityWitch.class,
        EntitySnowman.class,
        EntitySilverfish.class,
        EntityShulker.class,
        EntityGiantZombie.class,
        EntityGhast.class,
        EntityEndermite.class,
        EntityEnderman.class,
        EntityCaveSpider.class,
        AbstractSkeleton.class,
        EntityTNTPrimed.class,
        EntitySpider.class,
})
public abstract class EntitySubclassesEyeHeightMixin extends EntityMixin {

    @ModifyConstant(method = "getEyeHeight")
    float getEyeHeight(float constant) {
        return (float) (constant * $cm$size);
    }
}
