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
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

public final class PatchContext implements PatchContextDsl {

    private final List<Modifier> modifiers = new ArrayList<>();
    private final List<Pair<String, Type>> locals = new ArrayList<>();
    private final MethodPatcher parent;

    private boolean dump = false;

    PatchContext(MethodPatcher parent) {
        this.parent = parent;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public List<Pair<String, Type>> getLocals() {
        return locals;
    }

    public boolean isDump() {
        return dump;
    }

    @Override
    public void addLocal(String name, Type type) {
        locals.add(Pair.of(name, type));
    }

    @Override
    public void modify(ModifierType type, Anchor anchor, int nth, Hook hook) {
        modifiers.add(new Modifier(parent, anchor, hook, type, nth));
    }

    @Override
    public void debugDump() {
        dump = true;
    }

    @Override
    public String getClassName() {
        return parent.getParent().getClassName().replace('.', '/');
    }
}
