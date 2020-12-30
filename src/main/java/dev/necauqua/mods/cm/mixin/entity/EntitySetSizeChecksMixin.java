package dev.necauqua.mods.cm.mixin.entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({EntityAgeable.class, EntityZombie.class})
public abstract class EntitySetSizeChecksMixin extends EntityMixin {

    // fix them not calling super.setSize at first call of setSize
    @ModifyVariable(method = "setSize", at = @At("STORE"))
    boolean setSize(boolean var) {
        return true;
    }
}
