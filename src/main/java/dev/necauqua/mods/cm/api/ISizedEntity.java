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

package dev.necauqua.mods.cm.api;

import net.minecraft.entity.Entity;

/**
 * The {@link Entity} class is modified by the coremod
 * to implement this interface if it was loaded successfully.
 */
public interface ISizedEntity {

    /**
     * @return the size multiplier of an entity
     */
    default double getEntitySize() {
        return getEntitySize(1.0f);
    }

    /**
     * @param partialTick the interpolation factor
     * @return the interpolated ("smooth") multiplier of the entity size, used for rendering
     */
    double getEntitySize(float partialTick);

    /**
     * A shortcut for {@link #setEntitySize(double, boolean) setEntitySize(size, false)}.
     */
    default void setEntitySize(double size) {
        setEntitySize(size, false);
    }

    /**
     * Sets the entity size field or starts/changes a resizing process,
     * depending on the provided boolean, with no network synchronization -
     * that is, only on one side.
     * <p>
     * Also it does not change entity mounting (the )
     entity.dismountRidingEntity();
     entity.removePassengers();
     *
     * @param size        the size to set
     * @param interpolate <code>true</code to start a resize animation or
     *                    <code>false</code> set the size immediately
     */
    void setEntitySize(double size, boolean interpolate);
}
