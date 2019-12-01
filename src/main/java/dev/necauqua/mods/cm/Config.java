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

package dev.necauqua.mods.cm;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class Config {

    private Config() {}

    public static boolean changeBedAABB;
    public static boolean changePortalAABB;
    public static boolean enableSupersmalls;
    public static boolean enableBigSizes;
    public static float recalibratorEntityReachDist;
    public static boolean recalibratorItemEntityBBoxOffset;

    public static boolean scaleSmall, scaleBig;

    private static void load(Configuration c) {
        changeBedAABB = c.getBoolean("bedBBox", "misc", true,
            "Override vanilla bed bounding box so if you're small enough you can walk under it");

        changePortalAABB = c.getBoolean("portalBBox", "misc", true,
            "By default you are starting to use portal if you collide with blockspace it takes. " +
                "This option fixes that so if you're small you could walk on obsidian but not in portal");

        enableSupersmalls = c.getBoolean("enableSupersmalls", "main", true,
            "At these sizes (most noticeable at 1/4096) Minecraft starts to break a little so beware " +
                "of various (mostly visual, mob AI and colliding) glitches");

        enableBigSizes = c.getBoolean("enableBigs", "main", true,
            "Big sizes are OP and bugged even more then small.");

        recalibratorEntityReachDist = c.getFloat("recalibratorReach", "main", 64.0f, 0.0f, 256.0f,
            "How far (in blocks) the recalibrator can reach to change entity size. Can set to 0 to disable changing entities at all");

        recalibratorItemEntityBBoxOffset = c.getBoolean("recalibratorItemEntityBBoxOffset", "misc", true,
            "Item entities have their bboxes exactly below their models. You can check this with F3+B. " +
                "When this is true, recalibrator would take that into account and for item entities you would click " +
                "on rendering item and not below it");

        int fallEffect = c.getInt("fallEffect", "main", 1, 0, 3,
            "Specifies how falling damage applies. It is in range 0-4, so it's two bits - " +
                "first bit controls if fall damage is increased appropriately when you small, " +
                "and second - if it's descreased when you're big. " +
                "So 00=0 - realistic (when you small you can fall long, when you big your mass hurts you), " +
                "01=1 - comfortable (default but unrealistic, you fall long when small and fall normal when big), " +
                "10=2 - not really usable (small - normal, big - mass hurts), " +
                "11=3 - just scale (so it always feels as you're of normal size).");

        scaleSmall = (fallEffect >> 1) == 1;
        scaleBig = (fallEffect & 1) == 1;
    }

    public static void init(File configFolder) {
        File file = new File(configFolder, ChiseledMe.MODID + ".cfg");
        Configuration c = new Configuration(file);
        c.load();
        load(c);
        c.save();
    }
}
