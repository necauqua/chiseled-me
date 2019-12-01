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

/**
 * Entity class is modified to implement this interface if the mod is loaded.
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
     * Sets the entity size field, with no network synchronization or
     * @param size the size to set
     * @param interpolate <code>true</code to start a resize animation or
     *                    <code>false</code> set the size immediately
     */
    void setEntitySize(double size, boolean interpolate);
}
