package dev.necauqua.mods.mira.extras;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public interface IPreciseEntityType {

    Entity spawnSized(ServerWorld level, @Nullable ItemStack stack, @Nullable PlayerEntity player, BlockPos blockPos, SpawnReason reason, double size, BlockRayTraceResult target, boolean inside);
}
