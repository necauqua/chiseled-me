/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.Config;
import dev.necauqua.mods.mira.api.IWorldPreciseEvents;
import dev.necauqua.mods.mira.api.IWorldPreciseSounds;
import dev.necauqua.mods.mira.extras.IPreciseEffectPacket;
import dev.necauqua.mods.mira.size.ScaledParticleData;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(ClientPlayNetHandler.class)
public abstract class ClientPlayNetHandlerMixin {

    @Redirect(method = "handleLevelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;levelEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
    void handleEffect(ClientWorld self, int type, BlockPos pos, int data, SPlaySoundEventPacket packet) {
        IPreciseEffectPacket p = (IPreciseEffectPacket) packet;
        if (p.getCoordsCM() != null) {
            ((IWorldPreciseEvents) self).levelEvent(null, type, pos, data, p.getSizeCM(), p.getCoordsCM());
        } else {
            self.levelEvent(type, pos, data);
        }
    }

    @Redirect(method = "handleSoundEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    void handleSoundEffect(ClientWorld self, @Nullable PlayerEntity player, double x, double y, double z,
                           SoundEvent sound, SoundCategory category, float volume, float pitch, SPlaySoundEffectPacket packet) {
        IPreciseEffectPacket p = (IPreciseEffectPacket) packet;
        Vector3d coords = p.getCoordsCM();
        if (coords != null && Config.scaleSounds.get()) {
            ((IWorldPreciseSounds) self).playSound(player, coords, sound, category, volume, pitch, p.getSizeCM());
        } else {
            self.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @ModifyVariable(method = "handleParticleEvent", at = @At("STORE"), ordinal = 0)
    double handleParticleEventX(double d1, SSpawnParticlePacket packet) {
        return d1 * ScaledParticleData.getSize(packet.getParticle());
    }

    @ModifyVariable(method = "handleParticleEvent", at = @At("STORE"), ordinal = 1)
    double handleParticleEventY(double d1, SSpawnParticlePacket packet) {
        return d1 * ScaledParticleData.getSize(packet.getParticle());
    }

    @ModifyVariable(method = "handleParticleEvent", at = @At("STORE"), ordinal = 2)
    double handleParticleEventZ(double d1, SSpawnParticlePacket packet) {
        return d1 * ScaledParticleData.getSize(packet.getParticle());
    }
}
