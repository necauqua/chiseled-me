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
import org.objectweb.asm.util.Printer;

import static necauqua.mods.cm.asm.dsl.ModifierType.*;

public final class MethodInsnAnchor implements Anchor {

    private final int opcode;
    private final String owner;
    private final String name;
    private final String desc;

    public MethodInsnAnchor(int opcode, String owner, String name, String desc) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public SpecialMethodVisitor apply(SpecialMethodVisitor parent, CheckedHook hook, ModifierType type, int at) {
        return new SpecialMethodVisitor(parent) {
            private int n = 0;

            @Override
            public void visitMethodInsn(int op, String _owner, String _name, String _desc, boolean isInterface) {
                if (type == INSERT_AFTER) {
                    mv.visitMethodInsn(op, _owner, _name, _desc, isInterface);
                }
                if (op == opcode && _owner.equals(owner) && _name.equals(name) && _desc.equals(desc) && (at == ++n || at == 0)) {
                    parent.setPass(n);
                    hook.accept(parent);
                } else if (type == REPLACE) {
                    mv.visitMethodInsn(op, _owner, _name, _desc, isInterface);
                }
                if (type == INSERT_BEFORE) {
                    mv.visitMethodInsn(op, _owner, _name, _desc, isInterface);
                }
            }
        };
    }

    @Override
    public String toString() {
        return Printer.OPCODES[opcode] + " " + owner + "." + name + desc;
    }
}
