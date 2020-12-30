package dev.necauqua.mods.cm.mixin.compat;

import com.mojang.authlib.GameProfile;
import dev.necauqua.mods.cm.api.ISized;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "micdoodle8.mods.galacticraft.core.entities.player.GCEntityClientPlayerMP")
public abstract class GCEntityClientPlayerMPMixin extends EntityPlayer implements ISized {

    public GCEntityClientPlayerMPMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "getEyeHeight()F", at = @At("RETURN"), cancellable = true)
    void getEyeHeight(CallbackInfoReturnable<Float> ci) {
        ci.setReturnValue((float) (ci.getReturnValueF() * getSizeCM()));
    }

    // scale the distance between sending movement packets
    @ModifyConstant(method = "onLivingUpdate()V", constant = {
            @Constant(doubleValue = 0.00001),
            @Constant(doubleValue = 0.5, ordinal = 0),
            @Constant(doubleValue = 0.5, ordinal = 1),
            @Constant(doubleValue = 0.5, ordinal = 2),
            @Constant(doubleValue = 0.5, ordinal = 3), // without the fifth one
            @Constant(doubleValue = 0.5, ordinal = 5),
            // without the first one
            @Constant(doubleValue = 1.0, ordinal = 1),
            @Constant(doubleValue = 1.0, ordinal = 2),
            @Constant(doubleValue = 1.0, ordinal = 3),
            @Constant(doubleValue = 1.0, ordinal = 4),
            @Constant(doubleValue = 1.0, ordinal = 5),
    })
    double onLivingUpdate(double constant) {
        return constant * getSizeCM();
    }
}
