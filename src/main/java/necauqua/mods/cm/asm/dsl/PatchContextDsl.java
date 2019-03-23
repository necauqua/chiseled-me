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

import static necauqua.mods.cm.asm.dsl.ModifierType.*;

public interface PatchContextDsl {

    void addLocal(String name, Type type);

    void modify(ModifierType type, Anchor anchor, int nth, Hook hook);

    default void modify(ModifierType type, Anchor anchor, Hook hook) {
        modify(type, anchor, 1, hook);
    }

    default void insertBefore(Anchor anchor, Hook hook) {
        modify(INSERT_BEFORE, anchor, hook);
    }

    default void insertBefore(Anchor anchor, int nth, Hook hook) {
        modify(INSERT_BEFORE, anchor, nth, hook);
    }

    default void insertBeforeAll(Anchor anchor, Hook hook) {
        insertBefore(anchor, 0, hook);
    }

    default void insertAfter(Anchor anchor, Hook hook) {
        modify(INSERT_AFTER, anchor, hook);
    }

    default void insertAfter(Anchor anchor, int nth, Hook hook) {
        modify(INSERT_AFTER, anchor, nth, hook);
    }

    default void insertAfterAll(Anchor anchor, Hook hook) {
        insertAfter(anchor, 0, hook);
    }

    default void replace(Anchor anchor, Hook hook) {
        modify(REPLACE, anchor, hook);
    }

    default void replace(Anchor anchor, int nth, Hook hook) {
        modify(REPLACE, anchor, nth, hook);
    }

    default void replaceAll(Anchor anchor, Hook hook) {
        replace(anchor, 0, hook);
    }
}
