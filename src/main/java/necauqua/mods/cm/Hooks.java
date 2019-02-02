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

import necauqua.mods.cm.asm.CalledFromASM;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** This class holds methods that are called from ASM'ed minecraft code **/

@CalledFromASM
public final class Hooks {

    private Hooks() {}

    @CalledFromASM
    public static float getSize(Entity entity) {
        return EntitySizeManager.getData(entity).getSize();
    }

    @CalledFromASM
    @SideOnly(Side.CLIENT)
    public static float getRenderSize(Entity entity, float partialTick) {
        return EntitySizeManager.getData(entity).getRenderSize(partialTick);
    }

    @CalledFromASM
    @SideOnly(Side.CLIENT)
    public static float getScreenInterpEyeHeight(Entity entity, float partialTick) {
        return entity.getEyeHeight() / getSize(entity) * getRenderSize(entity, partialTick); // assuming getEyeHeight is patched already
    }

    @CalledFromASM
    public static void updateSize(Entity entity) {
        EntitySizeManager.getData(entity).tick();
    }

    @CalledFromASM
    public static float getLabelHeight(Entity entity, float old) {
        float off = entity.isSneaking() ?
            0.25F :
            0.5F;
        return (old - off) / getSize(entity) + off;
    }

    @CalledFromASM
    public static boolean cancelRunningParticlesHook(Entity entity) {
        return getSize(entity) <= 0.25F; // 1/4
    }

    @CalledFromASM
    public static boolean cancelBlockCollision(Entity entity, IBlockState state, BlockPos pos) { // this makes nether portal more fun
        return Config.changePortalAABB && state.getBlock() == Blocks.PORTAL && getSize(entity) < 1.0F && !entity.getEntityBoundingBox().intersectsWith(state.getSelectedBoundingBox(entity.world, pos));
    }
}
