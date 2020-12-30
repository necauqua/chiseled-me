package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.size.EntitySizeInteractions;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ParticleManager.class)
public final class ParticleManagerMixin {

    @Redirect(method = "spawnEffectParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/IParticleFactory;createParticle(ILnet/minecraft/world/World;DDDDDD[I)Lnet/minecraft/client/particle/Particle;"))
    Particle spawnEffectParticle(IParticleFactory factory, int particleId, World worldIn, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int[] parameters) {
        Particle particle = factory.createParticle(particleId, this.world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
        if (particle == null) {
            return null;
        }
        double size = EntitySizeInteractions.extractSize(particleId, parameters);
        if (size != 1.0) {
            ((ISized) particle).setSizeCM(size);
        }
        return particle;
    }

    @Shadow
    protected World world;
}
