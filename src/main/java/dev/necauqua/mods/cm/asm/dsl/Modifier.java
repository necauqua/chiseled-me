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

package dev.necauqua.mods.cm.asm.dsl;

import dev.necauqua.mods.cm.asm.dsl.anchors.Anchor;

public final class Modifier {

    private final MethodPatcher parent;
    private final ModifierType type;
    private final Anchor anchor;
    private final int index;
    private final Hook hook;

    private boolean matched = false;

    public Modifier(MethodPatcher parent, Anchor anchor, Hook hook, ModifierType type, int index) {
        this.parent = parent;
        this.type = type;
        this.anchor = anchor;
        this.index = index;
        this.hook = hook;
    }

    public MethodPatcher getParent() {
        return parent;
    }

    public ModifierType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public Hook getHook() {
        return hook;
    }

    public boolean didMatch() {
        return matched;
    }

    public ContextMethodVisitor apply(ContextMethodVisitor parent) {
        return anchor.apply(parent, this);
    }

    public boolean tryRun(ContextMethodVisitor context, int pass) {
        if (index != 0 && index != pass) {
            return false;
        }
        matched = true;
        context.setPass(pass);
        hook.accept(context);
        context.setPass(1);
        return true;
    }

    @Override
    public String toString() {
        return "<" + type + (index == 0 ?
            " all" :
            "") + " " + anchor + (index > 1 ?
            " at " + index :
            "") + ">";
    }
}
