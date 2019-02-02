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
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Patch implements PatchWithLocalsDsl {

    protected final Map<String, Type> locals = new HashMap<>();
    protected final List<Modifier> modifiers = new ArrayList<>();

    Patch() {}

    public Map<String, Type> getLocals() {
        return locals;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public PatchWithLocalsDsl addLocal(String assocName, Type type) {
        locals.put(assocName, type);
        return this;
    }

    @Override
    public PatchDsl modify(ModifierType type, Anchor anchor, int nth, AsmMethodHook hook) {
        modifiers.add(new Modifier(this, type, anchor, nth, hook));
        return this;
    }
}
