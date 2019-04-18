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

package dev.necauqua.mods.cm.asm.dsl.anchors;

import dev.necauqua.mods.cm.asm.dsl.ContextMethodVisitor;
import dev.necauqua.mods.cm.asm.dsl.Modifier;
import dev.necauqua.mods.cm.asm.dsl.ModifierType;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import static dev.necauqua.mods.cm.asm.dsl.ModifierType.*;

public abstract class Anchor {

    public abstract ContextMethodVisitor apply(ContextMethodVisitor context, Modifier modifier);

    protected final void visit(Modifier modifier, ContextMethodVisitor context, Runnable original, BooleanSupplier matches, IntSupplier pass) {
        ModifierType type = modifier.getType();
        if (type == INSERT_AFTER) {
            original.run();
        }
        if (!(matches.getAsBoolean() && modifier.match(context, pass.getAsInt()))) {
            if (type == REPLACE) {
                original.run();
            }
        }
        if (type == INSERT_BEFORE) {
            original.run();
        }
    }
}
