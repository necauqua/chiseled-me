/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm;

import dev.necauqua.mods.cm.api.IRenderSized;
import dev.necauqua.mods.cm.size.ChangingSizeProcess;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static dev.necauqua.mods.cm.ChiseledMe.LOWER_LIMIT;
import static dev.necauqua.mods.cm.ChiseledMe.UPPER_LIMIT;
import static java.lang.String.format;

public final class SizeofCommand extends CommandBase {

    @Override
    @Nonnull
    public String getName() {
        return "sizeof";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.chiseled_me:sizeof.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length == 2 || args.length < 1 || args.length > 5) {
            throw new WrongUsageException(getUsage(sender));
        }
        Entity entity = getEntity(server, sender, args[0]);
        if (args.length == 1) {
            sender.sendMessage(new TextComponentTranslation("commands.chiseled_me:sizeof.get", entity.getDisplayName(), ((IRenderSized) entity).getSizeCM()));
            return;
        }

        double size = ((IRenderSized) entity).getSizeCM();
        double arg = parseDouble(args[2]);

        switch (args[1]) {
            case "set":
                size = arg;
                break;
            case "add":
                size += arg;
                break;
            case "subtract":
                size -= arg;
                break;
            case "multiply":
                size *= arg;
                break;
            case "divide":
                size /= arg;
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
        if (!Config.allowAnySizes) {
            if (size < LOWER_LIMIT) {
                throw new NumberInvalidException("commands.generic.num.tooSmall", format("%.2f", size), LOWER_LIMIT);
            } else if (size > UPPER_LIMIT) {
                throw new NumberInvalidException("commands.generic.num.tooBig", format("%.2f", size), UPPER_LIMIT);
            }
        }
        int lerpTime = 0;
        if (args.length > 3 && !args[3].equals("animate")) {
            throw new WrongUsageException(getUsage(sender));
        }
        if (args.length == 4) {
            lerpTime = ChangingSizeProcess.log2LerpTime(((IRenderSized) entity).getSizeCM(), size);
        } else if (args.length == 5) {
            lerpTime = parseInt(args[4], 0);
        }
        ((IRenderSized) entity).setSizeCM(size, lerpTime);
        sender.sendMessage(new TextComponentTranslation("commands.chiseled_me:sizeof.set", entity.getDisplayName(), size));
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        String[] completions;
        switch (args.length) {
            case 1:
                completions = server.getOnlinePlayerNames();
                break;
            case 2:
                completions = new String[]{"set", "add", "subtract", "multiply", "divide"};
                break;
            case 4:
                completions = new String[]{"animate"};
                break;
            default:
                completions = new String[0];
        }
        return getListOfStringsMatchingLastWord(args, completions);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }
}
