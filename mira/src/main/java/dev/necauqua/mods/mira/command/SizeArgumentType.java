/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.necauqua.mods.mira.Config;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Arrays;
import java.util.Collection;

import static dev.necauqua.mods.mira.Mira.*;

@EventBusSubscriber(bus = Bus.MOD)
public final class SizeArgumentType implements ArgumentType<Double> {

    private static final Collection<String> EXAMPLES = Arrays.asList("1", ".5", "1/8", "2");

    @SubscribeEvent
    public static void on(FMLCommonSetupEvent e) {
        ArgumentTypes.register(ns("mira").toString(), SizeArgumentType.class, new IArgumentSerializer<SizeArgumentType>() {
            @Override
            public void serializeToNetwork(SizeArgumentType argument, PacketBuffer buffer) {
            }

            @Override
            public SizeArgumentType deserializeFromNetwork(PacketBuffer buffer) {
                return sizeArg();
            }

            @Override
            public void serializeToJson(SizeArgumentType arg, JsonObject json) {
            }
        });
    }

    public static SizeArgumentType sizeArg() {
        return new SizeArgumentType();
    }

    @Override
    public Double parse(final StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        boolean invert = reader.canRead(2) && reader.peek() == '1' && reader.peek(1) == '/';
        if (invert) {
            reader.skip();
            reader.skip();
        }
        double result = reader.readDouble();
        if (invert) {
            result = 1.0 / result;
        }
        if (Config.allowAnySizes.get()) {
            return result;
        }
        if (result < LOWER_LIMIT) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().createWithContext(reader, result, LOWER_LIMIT);
        }
        if (result > UPPER_LIMIT) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().createWithContext(reader, result, UPPER_LIMIT);
        }
        return result;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public String toString() {
        return "size()";
    }
}
