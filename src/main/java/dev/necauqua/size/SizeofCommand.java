/*
 * Copyright (c) 2016-2019 Anton Bulakh <necauqua@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.necauqua.size;

import dev.necauqua.size.api.IRenderSized;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.String.format;

public final class SizeofCommand extends CommandBase {

    public static final float LOWER_LIMIT = 0.000244140625f; // = 1/16/16/16 = 1/4096

    @Override
    @Nonnull
    public String getName() {
        return "sizeof";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.size:sizeof.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length == 2 || args.length < 1 || args.length > 5) {
            throw new WrongUsageException(getUsage(sender));
        }
        Entity entity = getEntity(server, sender, args[0]);
        if (args.length == 1) {
            sender.sendMessage(new TextComponentTranslation("commands.size:sizeof.get", entity.getDisplayName(), ((IRenderSized) entity).getSize()));
            return;
        }

        double size = ((IRenderSized) entity).getSize();
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
        if (size < LOWER_LIMIT) {
            throw new NumberInvalidException("commands.generic.num.tooSmall", format("%.2f", size), LOWER_LIMIT);
        } else if (size > Config.maxSize) {
            throw new NumberInvalidException("commands.generic.num.tooBig", format("%.2f", size), Config.maxSize);
        }
        int lerpTime = 0;
        if (args.length > 3 && !args[3].equals("animate")) {
            throw new WrongUsageException(getUsage(sender));
        }
        if (args.length == 4) {
            lerpTime = log2LerpTime(((IRenderSized) entity).getSize(), size);
        } else if (args.length == 5) {
            lerpTime = parseInt(args[4], 0);
        }
        ((IRenderSized) entity).setSize(size, lerpTime);
        sender.sendMessage(new TextComponentTranslation("commands.size:sizeof.set", entity.getDisplayName(), size));
    }

    private static final double TWO_OVER_LOG_TWO = 2.0 / log(2);

    public static int log2LerpTime(double fromSize, double toSize) {
        return (int) (abs(log(fromSize) - log(toSize)) * TWO_OVER_LOG_TWO);
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
