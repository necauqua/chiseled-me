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

import static necauqua.mods.cm.asm.dsl.ModifierType.*;

public interface PatchDsl {

    PatchDsl modify(ModifierType type, Anchor anchor, int nth, AsmMethodHook hook);

    default PatchDsl modify(ModifierType type, Anchor anchor, AsmMethodHook hook) {
        return modify(type, anchor, 1, hook);
    }

    default PatchDsl insertBefore(Anchor anchor, AsmMethodHook hook) {
        return modify(INSERT_BEFORE, anchor, hook);
    }

    default PatchDsl insertBefore(Anchor anchor, int nth, AsmMethodHook hook) {
        return modify(INSERT_BEFORE, anchor, nth, hook);
    }

    default PatchDsl insertBeforeAll(Anchor anchor, AsmMethodHook hook) {
        return insertBefore(anchor, 0, hook);
    }

    default PatchDsl insertAfter(Anchor anchor, AsmMethodHook hook) {
        return modify(INSERT_AFTER, anchor, hook);
    }

    default PatchDsl insertAfter(Anchor anchor, int nth, AsmMethodHook hook) {
        return modify(INSERT_AFTER, anchor, nth, hook);
    }

    default PatchDsl insertAfterAll(Anchor anchor, AsmMethodHook hook) {
        return insertAfter(anchor, 0, hook);
    }

    default PatchDsl replace(Anchor anchor, AsmMethodHook hook) {
        return modify(REPLACE, anchor, hook);
    }

    default PatchDsl replace(Anchor anchor, int nth, AsmMethodHook hook) {
        return modify(REPLACE, anchor, nth, hook);
    }

    default PatchDsl replaceAll(Anchor anchor, AsmMethodHook hook) {
        return replace(anchor, 0, hook);
    }
}
