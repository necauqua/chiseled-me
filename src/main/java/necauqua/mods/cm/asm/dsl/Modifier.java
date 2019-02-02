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

package necauqua.mods.cm.asm.dsl;

import necauqua.mods.cm.asm.dsl.anchors.Anchor;

public final class Modifier {

    private final ModifierType type;
    private final Patch parent;
    private final Anchor anchor;

    private final int nth;
    private CheckedHook hook;

    public Modifier(Patch parent, ModifierType type, Anchor anchor, int nth, AsmMethodHook hook) {
        this.type = type;
        this.parent = parent;
        this.anchor = anchor;
        this.nth = nth;
        this.hook = new CheckedHook(hook);
    }

    public CheckedHook getHook() {
        return hook;
    }

    public SpecialMethodVisitor apply(SpecialMethodVisitor parent) {
        parent.setPass(1);
        if (hook == null) {
            throw new IllegalStateException("Modifier " + toString() + " has no 'code' block!");
        }
        return anchor.apply(parent, hook, type, nth);
    }

    @Override
    public String toString() {
        return "<" + type + (nth == 0 ?
            " all" :
            "") + " " + anchor + (nth > 1 ?
            " at " + nth :
            "") + ">";
    }
}
