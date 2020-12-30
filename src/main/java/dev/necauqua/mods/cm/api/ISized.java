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
 * An interface describing something <i>sized</i>, meaning it has a size attribute of type double.
 * This is usually implemented on vanilla types (Entities, Particles etc.) by a mixin.
 * <p>
 * See {@link IRenderSized} for more description and for example usage as a soft dependency.
 * @see IRenderSized
 * @author necauqua
 */
public interface ISized {

    /**
     * @return the size multiplier of this thing
     */
    double getSizeCM();

    /**
     * Sets the size of the this thing.
     * <p>
     * <b>NO CHECKS ARE DONE FOR SIZE CORRECTNESS</b>
     * (e.g. you can set it to NaN or negative and get a bunch of unexpected behavior).
     *
     * @param size the size to be set
     */
    void setSizeCM(double size);
}
