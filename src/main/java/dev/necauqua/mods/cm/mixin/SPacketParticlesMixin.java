/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.mixin;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.objectweb.asm.Opcodes.ISTORE;

// patch the packet to allow particle arguments array to be any length
@Mixin(SPacketParticles.class)
public final class SPacketParticlesMixin {

    @ModifyVariable(method = "readPacketData", at = @At(value = "STORE", opcode = ISTORE), ordinal = 0)
    int readPacketData(int _original, PacketBuffer buffer) {
        return buffer.readVarInt();
    }

    @ModifyVariable(method = "writePacketData", at = @At(value = "STORE", opcode = ISTORE), ordinal = 0)
    int writePacketData(int _original, PacketBuffer buffer) {
        buffer.writeVarInt(particleArguments.length);
        return particleArguments.length;
    }

    @Shadow
    private int[] particleArguments;
}
