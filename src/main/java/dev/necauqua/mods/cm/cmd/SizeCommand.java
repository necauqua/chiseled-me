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

package dev.necauqua.mods.cm.cmd;

import dev.necauqua.mods.cm.size.EntitySizeManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static dev.necauqua.mods.cm.size.EntitySizeManager.*;

public final class SizeCommand extends CommandBase {

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
        if (args.length < 2 || args.length > 4) {
            throw new WrongUsageException(getUsage(sender));
        }
        Entity entity = getEntity(server, sender, args[0]);
        if (args.length == 2) {
            if (!"get".equals(args[1])) {
                throw new WrongUsageException(getUsage(sender));
            }
            sender.sendMessage(new TextComponentTranslation("commands.chiseled_me:sizeof.get", entity.getDisplayName(), getSize(entity)));
            return;
        }

        boolean animate = args.length == 4 && args[3].matches("t|true|y|yes|1");
        double size = getSize(entity);

        switch (args[1]) {
            case "set":
                size = parseDouble(args[2], LOWER_LIMIT, UPPER_LIMIT);
                break;
            case "add":
                size += parseDouble(args[2], LOWER_LIMIT, UPPER_LIMIT);
                break;
            case "subtract":
                size -= parseDouble(args[2], LOWER_LIMIT, UPPER_LIMIT);
                break;
            case "multiply":
                size *= parseDouble(args[2], LOWER_LIMIT, UPPER_LIMIT);
                break;
            case "divide":
                size /= parseDouble(args[2], LOWER_LIMIT, UPPER_LIMIT);
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }

        EntitySizeManager.setSizeAndSync(entity, size, animate);
        sender.sendMessage(new TextComponentTranslation("commands.chiseled_me:sizeof.set", entity.getDisplayName(), size));
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        String[] completions;
        switch (args.length) {
            case 1:
                completions =  server.getOnlinePlayerNames();
                break;
            case 2:
                completions = new String[]{ "get", "set", "add", "subtract", "multiply", "divide" };
                break;
            case 4:
                completions = "get".equals(args[2]) ? new String[0] : new String[]{"true", "false"};
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
