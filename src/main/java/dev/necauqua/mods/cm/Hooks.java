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

import dev.necauqua.mods.cm.asm.CalledFromASM;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import static dev.necauqua.mods.cm.EntitySizeManager.getSize;

/** This class holds methods that are called from ASM'ed minecraft code **/

@CalledFromASM
public final class Hooks {

    private Hooks() {}

    @CalledFromASM
    public static boolean cancelBlockCollision(Entity entity, IBlockState state, BlockPos pos) { // this makes nether portal more fun
        return Config.changePortalAABB && state.getBlock() == Blocks.PORTAL && getSize(entity) < 1.0F
            && !entity.getEntityBoundingBox().intersects(state.getSelectedBoundingBox(entity.world, pos));
    }
}
