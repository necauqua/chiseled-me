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

package necauqua.mods.cm;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;

public final class Config {

    private Config() {}

    public static boolean changeBedAABB;
    public static boolean changePortalAABB;
    public static boolean enableSupersmalls;
    public static boolean enableBigSizes;

    private static void load(Configuration c) {
        changeBedAABB = c.getBoolean("bedBBox", "misc", true,
            "Override vanilla bed bounding box so if you're small enough you can walk under it");
        changePortalAABB = c.getBoolean("portalBBox", "misc", true,
            "By default you are starting to use portal if you collide with blockspace it takes. This option fixes that so if you're small you could walk on obsidian but not in portal");
        enableSupersmalls = c.getBoolean("enableSupersmalls", "main", true,
            "At these sizes (most noticeable at 1/4096) Minecraft starts to break a little so beware of various (mostly visual, mob AI and colliding) glitches");
        enableBigSizes = c.getBoolean("enableBigs", "main", true,
            "Big sizes are OP and bugged even more then small.");
    }

    public static void init(File configFolder) {
        try {
            File file = new File(configFolder, "chiseled_me.cfg");
            file.createNewFile();
            Configuration c = new Configuration(file);
            c.load();
            load(c);
            c.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
