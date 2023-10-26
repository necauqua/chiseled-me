/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.data;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.IRuleEntryVisitor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public final class DoubleValue extends GameRules.RuleValue<DoubleValue> {

    private double value;

    public DoubleValue(GameRules.RuleType<DoubleValue> type, double defaultValue) {
        super(type);
        value = defaultValue;
    }

    public static GameRules.RuleType<DoubleValue> create(double defaultValue) {
        return create(defaultValue, (server, value) -> {
        });
    }

    public static GameRules.RuleType<DoubleValue> create(double defaultValue, BiConsumer<MinecraftServer, DoubleValue> changeListener) {
        return new GameRules.RuleType<>(DoubleArgumentType::doubleArg, type -> new DoubleValue(type, defaultValue), changeListener, IRuleEntryVisitor::visit);
    }

    @Override
    protected void updateFromArgument(CommandContext<CommandSource> context, String paramName) {
        value = IntegerArgumentType.getInteger(context, paramName);
    }

    @Override
    protected void deserialize(String value) {
        double result = 0;
        if (!value.isEmpty()) {
            try {
                result = Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
            }
        }
        this.value = result;
    }

    @Override
    public String serialize() {
        return Double.toString(value);
    }

    @Override
    public int getCommandResult() {
        return (int) value;
    }

    @Override
    protected DoubleValue getSelf() {
        return this;
    }

    @Override
    protected DoubleValue copy() {
        return new DoubleValue(type, value);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setFrom(DoubleValue value, @Nullable MinecraftServer server) {
        this.value = value.value;
        onChanged(server);
    }

    public double get() {
        return value;
    }
}
