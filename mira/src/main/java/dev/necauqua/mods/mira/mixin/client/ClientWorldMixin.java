package dev.necauqua.mods.mira.mixin.client;

import dev.necauqua.mods.mira.api.IWorldPreciseEvents;
import dev.necauqua.mods.mira.api.IWorldPreciseSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements IWorldPreciseEvents, IWorldPreciseSounds {

    @Shadow
    @Final
    private WorldRenderer levelRenderer;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Override
    public void playLocalSound(Vector3d pos, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean distanceDelay, double size) {
        playLocalSound(pos.x, pos.y, pos.z, sound, category, volume, pitch, distanceDelay);
    }

    @Override
    public void levelEvent(PlayerEntity player, int type, BlockPos blockPos, int data, double size, Vector3d pos) {
        try {
            ((IWorldPreciseEvents) levelRenderer).levelEvent(player, type, blockPos, data, size, pos);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Playing level event");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Level event being played");
            crashreportcategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(blockPos));
            crashreportcategory.setDetail("Event source", player);
            crashreportcategory.setDetail("Event type", type);
            crashreportcategory.setDetail("Event data", data);
            throw new ReportedException(crashreport);
        }
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, Vector3d pos, SoundEvent sound, SoundCategory category, float volume, float pitch, double size) {
        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(player, sound, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) {
            return;
        }
        sound = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        if (player == minecraft.player) {
            playLocalSound(pos, sound, category, volume, pitch, false, size);
        }
    }

    @Shadow
    public abstract void levelEvent(@Nullable PlayerEntity player, int type, BlockPos blockPos, int data);


    @Shadow
    public abstract void playLocalSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean ignoreDistance);
}
