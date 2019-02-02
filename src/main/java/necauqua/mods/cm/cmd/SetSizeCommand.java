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

package necauqua.mods.cm.cmd;

import necauqua.mods.cm.EntitySizeManager;
import necauqua.mods.cm.Network;
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

public class SetSizeCommand extends CommandBase {

    @Override
    @Nonnull
    public String getName() {
        return "setsizeof";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.chiseled_me:setsizeof.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }
        float size = (float) parseDouble(args[1], EntitySizeManager.LOWER_LIMIT, EntitySizeManager.UPPER_LIMIT);
        boolean interp = true;
        if (args.length > 2) {
            interp = args[2].matches("t|true|y|yes");
        }
        Entity entity = getEntity(server, sender, args[0]);
        EntitySizeManager.setSize(entity, size, interp);
        Network.sendSetSizeToClients(entity, size, interp);
        sender.sendMessage(new TextComponentTranslation("commands.chiseled_me:setsizeof.message", entity.getDisplayName(), size));
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
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
