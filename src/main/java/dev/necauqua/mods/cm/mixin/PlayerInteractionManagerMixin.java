package dev.necauqua.mods.cm.mixin;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInteractionManager.class)
public final class PlayerInteractionManagerMixin {

    @Redirect(method = "tryHarvestBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V"))
    void playSound(World self, EntityPlayer player, int type, BlockPos pos, int data) {
        double size = ((ISized) player).getSizeCM();
        ((IWorldPreciseEvents) self).playEvent(player, type, pos, data, size, player.getPositionEyes(1.0f));
    }
}
