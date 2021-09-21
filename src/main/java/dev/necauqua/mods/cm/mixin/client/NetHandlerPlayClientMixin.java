/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.Config;
import dev.necauqua.mods.cm.api.IWorldPreciseEvents;
import dev.necauqua.mods.cm.api.IWorldPreciseSounds;
import dev.necauqua.mods.cm.size.IPreciseEffectPacket;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayClient.class)
public final class NetHandlerPlayClientMixin {

    @Redirect(method = "handleEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
    void handleEffect(WorldClient self, int type, BlockPos pos, int data, SPacketEffect packet) {
        IPreciseEffectPacket p = (IPreciseEffectPacket) packet;
        if (p.getCoordsCM() != null) {
            ((IWorldPreciseEvents) self).playEvent(null, type, pos, data, p.getSizeCM(), p.getCoordsCM());
        } else {
            self.playEvent(type, pos, data);
        }
    }

    @Redirect(method = "handleSoundEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    void handleSoundEffect(WorldClient self, EntityPlayer player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, SPacketSoundEffect packet) {
        IPreciseEffectPacket p = (IPreciseEffectPacket) packet;
        Vec3d coords = p.getCoordsCM();
        if (coords != null && Config.scaleSounds) {
            ((IWorldPreciseSounds) self).playSound(player, coords, sound, category, volume, pitch, p.getSizeCM());
        } else {
            self.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }
}
