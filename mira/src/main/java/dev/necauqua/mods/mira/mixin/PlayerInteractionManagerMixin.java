package dev.necauqua.mods.mira.mixin;

import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerInteractionManager.class)
public final class PlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    // stupid 1.5 y offset in creative
    @ModifyConstant(method = "handleBlockBreakAction", constant = @Constant(doubleValue = 1.5))
    double handleBlockBreakAction(double constant) {
        return constant * ((ISized) player).getSizeCM();
    }
}
