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

package necauqua.mods.cm.asm.dsl.anchors;

import necauqua.mods.cm.asm.dsl.CheckedHook;
import necauqua.mods.cm.asm.dsl.ModifierType;
import necauqua.mods.cm.asm.dsl.SpecialMethodVisitor;

public final class MethodBeginAnchor implements Anchor {

    public static final MethodBeginAnchor INSTANCE = new MethodBeginAnchor();

    private MethodBeginAnchor() {}

    @Override
    public SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, ModifierType type, int at) {
        return new SpecialMethodVisitor(parent) {
            @Override
            public void visitCode() {
                if (type == ModifierType.INSERT_AFTER) {
                    mv.visitCode();
                }
                hook.accept(parent);
                if (type == ModifierType.INSERT_BEFORE) {
                    mv.visitCode();
                }
            }
        };
    }

    @Override
    public String toString() {
        return "method begin";
    }
}
