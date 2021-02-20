package dev.necauqua.mods.cm.mixin.entity.monster;

import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import dev.necauqua.mods.cm.mixin.entity.EntityMixin;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityZombieVillager.class)
public abstract class EntityZombieVillagerMixin extends EntityMixin {

    @Redirect(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V"))
    void playEvent(World self, EntityPlayer player, int type, BlockPos pos, int data) {
        ((IWorldPreciseEvents) self).playEvent(null, type, pos, data, $cm$size, getPositionEyes(1.0f));
    }
}
