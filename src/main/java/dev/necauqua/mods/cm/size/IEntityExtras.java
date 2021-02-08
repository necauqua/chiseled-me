/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm.size;

public interface IEntityExtras {

    void setOriginalWidthCM(float width);

    void setOriginalHeightCM(float height);

    void onUpdateCM();
}
