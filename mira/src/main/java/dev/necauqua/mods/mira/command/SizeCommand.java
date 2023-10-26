/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.necauqua.mods.mira.Mira;
import dev.necauqua.mods.mira.api.IRenderSized;
import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static dev.necauqua.mods.mira.api.IResizingProgress.log2LerpTime;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static net.minecraft.command.ISuggestionProvider.matchesSubStr;
import static net.minecraft.command.arguments.EntityArgument.*;

@EventBusSubscriber(modid = Mira.MODID)
public final class SizeCommand {

    private static final String[] COMMON_SIZES = {"1", "1/2", "1/4", "1/8", "1/16", "2", "4", "8", "16", "1/32", "1/64", "1/128", "1/256", "1/512", "1/1024", "1/2048", "1/4096",};
    private static final SuggestionProvider<CommandSource> COMMON_SIZES_PROVIDER = SuggestionProviders.register(Mira.ns("common_sizes"), (context, builder) -> {
        String s = builder.getRemaining().toLowerCase(Locale.ROOT);
        StringRange range = StringRange.between(builder.getStart(), builder.getInput().length());
        List<Suggestion> result = Arrays.stream(COMMON_SIZES).filter(size -> matchesSubStr(s, size.toLowerCase(Locale.ROOT)) && !size.equals(builder.getRemaining())).map(size -> new Suggestion(range, size, () -> "some tooltip info kekw") {
            @Override
            public Suggestion expand(String command, StringRange range) {
                class SpecialSuggestionSortingLol extends Suggestion {

                    public SpecialSuggestionSortingLol(Suggestion original) {
                        super(original.getRange(), original.getText(), original.getTooltip());
                    }

                    @Override
                    public int compareToIgnoreCase(Suggestion o) {
                        return o instanceof SpecialSuggestionSortingLol ? 0 : Integer.MIN_VALUE;
                    }
                }
                return new SpecialSuggestionSortingLol(super.expand(command, range));
            }
        }).collect(Collectors.toList());
        return CompletableFuture.completedFuture(Suggestions.create(builder.getInput(), result));
    });
    private static final List<LiteralArgumentBuilder<CommandSource>> SUBCOMMANDS = asList(literal("get").executes(context -> executeForEntities(context, entity -> {
        double size = ((ISized) entity).getSizeCM();
        context.getSource().sendSuccess(tr("command.mira.size.get", entity.getDisplayName(), size), true);
    })), literal("set").then(buildSetter("to", (size, to) -> to, true)), literal("add").then(buildSetter("n", Double::sum /* lol */, false)), literal("sub").then(buildSetter("n", (size, n) -> size - n, false)), literal("mul").then(buildSetter("by", (size, by) -> size * by, false)), literal("div").then(buildSetter("by", (size, by) -> size / by, false)));
    private static final RequiredArgumentBuilder<CommandSource, EntitySelector> ENTITY_NODE = argument("entity", entities());

    private static final LiteralArgumentBuilder<CommandSource> HELP = literal("help").executes(context -> {
        CommandSource src = context.getSource();
        src.sendSuccess(helpCommandTitle(SizeCommand.HELP), true);
        src.sendSuccess(tr("command.mira.help.desc"), true);
        for (LiteralArgumentBuilder<CommandSource> subcommand : SizeCommand.SUBCOMMANDS) {
            src.sendSuccess(helpCommandTitle(subcommand), true);
            src.sendSuccess(tr("command.mira.size." + subcommand.getLiteral() + ".desc"), true);
        }
        return SINGLE_SUCCESS;
    });

    // size -> get size of self
    // size <number> -> set size of self
    // size <verb> <number> -> <verb> size of self
    // size of <selector> <verb> <number> -> <verb> size of entities that matched
    // <selector>
    private static final LiteralArgumentBuilder<CommandSource> SIZE =
        literal("size")
            .requires(src -> src.hasPermission(2))
            .executes(context -> {
                Entity entity = context.getSource().getEntity();
                if (entity == null) {
                    throw NO_ENTITIES_FOUND.create();
                }
                double size = ((ISized) entity).getSizeCM();
                context.getSource().sendSuccess(tr("command.mira.size.get", entity.getDisplayName(), size), true);
                return SINGLE_SUCCESS;
            })
            .then(HELP)
            .then(buildSetter("size", (size, to) -> to, true))
            .then(literal("of")
                .then(ENTITY_NODE));

    static {
        for (LiteralArgumentBuilder<CommandSource> subcommand : SUBCOMMANDS) {
            SIZE.then(subcommand);
            HELP.then(literal(subcommand.getLiteral()).executes(context -> {
                context.getSource().sendSuccess(tr("command.mira.size." + subcommand.getLiteral() + ".desc"), true);
                return 1;
            }));
            ENTITY_NODE.then(subcommand);
        }
    }

    @SubscribeEvent
    public static void on(RegisterCommandsEvent e) {
        e.getDispatcher().register(SIZE);
    }

    private static ITextComponent helpCommandTitle(LiteralArgumentBuilder<CommandSource> subcommand) {
        return new StringTextComponent("/size [of <entity>] " + subcommand.getLiteral()).withStyle(TextFormatting.GOLD).append(":");
    }

    private static RequiredArgumentBuilder<CommandSource, Double> buildSetter(String argName, DoubleBinaryOperator updateFunction, boolean suggest) {
        return argument(argName, SizeArgumentType.sizeArg()).suggests(
            suggest ? COMMON_SIZES_PROVIDER : null).executes(context -> {
            double arg = getDouble(context, argName);
            return executeForEntities(context, entity -> {
                double curSize = ((IRenderSized) entity).getSizeCM(0.0F);
                double size = updateFunction.applyAsDouble(curSize, arg);
                ((IRenderSized) entity).setSizeCM(size, 0);
                TranslationTextComponent msg = tr("command.mira.size.set", entity.getDisplayName(), size);
                context.getSource().sendSuccess(msg, true);
            });
        }).then(literal("animate").executes(context -> {
            double arg = getDouble(context, argName);
            return executeForEntities(context, entity -> {
                double curSize = ((IRenderSized) entity).getSizeCM(0.0F);
                double size = updateFunction.applyAsDouble(curSize, arg);
                int time = log2LerpTime(curSize, size);
                ((IRenderSized) entity).setSizeCM(size, time);
                TranslationTextComponent msg = tr("command.mira.size.set", entity.getDisplayName(), size);
                context.getSource().sendSuccess(msg, true);
            });
        }).then(argument("ticks", integer(0)).executes(context -> {
            double arg = getDouble(context, argName);
            int time = getInteger(context, "ticks");
            return executeForEntities(context, entity -> {
                double curSize = ((IRenderSized) entity).getSizeCM(0.0F);
                double size = updateFunction.applyAsDouble(curSize, arg);
                ((IRenderSized) entity).setSizeCM(size, time);
                TranslationTextComponent msg = tr("command.mira.size.set", entity.getDisplayName(), size);
                context.getSource().sendSuccess(msg, true);
            });
        })));
    }

    private static int executeForEntities(CommandContext<CommandSource> context, Consumer<Entity> action) throws CommandSyntaxException {
        Collection<? extends Entity> entities;
        try {
            entities = getEntities(context, "entity");
        } catch (IllegalArgumentException e) { // if no 'entity' argument
            Entity entity = context.getSource().getEntity();
            if (entity == null) {
                throw NO_ENTITIES_FOUND.create();
            }
            entities = singletonList(entity);
        }
        entities.forEach(action);
        return entities.size();
    }


    private static TranslationTextComponent tr(String key, Object... args) {
        return new TranslationTextComponent(key, args);
    }

    private static TranslationTextComponent tr(String key) {
        return new TranslationTextComponent(key);
    }
}
