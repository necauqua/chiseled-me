package dev.necauqua.mods.cm.mixin.client;

import dev.necauqua.mods.cm.api.ISized;
import dev.necauqua.mods.cm.api.IWorldPreciseSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(WorldClient.class)
public abstract class WorldClientMixin implements IWorldPreciseSounds {

    private double $cm$size = 1.0;

    @Override
    public void playSound(Vec3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay, double size) {
        $cm$size = size;
        playSound(pos.x, pos.y, pos.z, soundIn, category, volume, pitch, distanceDelay);
        $cm$size = 1.0;
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, Vec3d pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch, double size) {
        if (player == mc.player) {
            playSound(pos, soundIn, category, volume, pitch, false, size);
        }
    }

    @Redirect(method = "playSound(DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V", at = @At(value = "NEW", target = "net/minecraft/client/audio/PositionedSoundRecord"))
    PositionedSoundRecord playSound(SoundEvent sound, SoundCategory category, float volume, float pitch, float x, float y, float z) {
        PositionedSoundRecord s = new PositionedSoundRecord(sound, category, volume, pitch, x, y, z);
        //noinspection ConstantConditions we mixin the interface lol
        ((ISized) s).setSizeCM($cm$size);
        return s;
    }

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    public abstract void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay);
}
