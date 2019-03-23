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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

import static org.objectweb.asm.Opcodes.GOTO;

public class ContextMethodVisitor extends MethodVisitor {

    private final String className;
    private final Map<String, Integer> locals;
    private final MethodVisitor root;

    private int pass = 1; // used in `code` lambdas here and there

    public ContextMethodVisitor(String className, Map<String, Integer> locals, MethodVisitor parent, MethodVisitor root) {
        super(Opcodes.ASM5, parent);
        this.className = className.replace('.', '/');
        this.locals = locals;
        this.root = root;
    }

    public ContextMethodVisitor(ContextMethodVisitor mv) {
        super(Opcodes.ASM5, mv);
        className = mv.className;
        locals = mv.locals;
        root = mv.root;
    }

    public void setPass(int pass) {
        this.pass = pass;
    }

    public int getPass() {
        return pass;
    }

    // Additional methods:

    public void visitHook(Hook hook) {
        hook.accept(this);
    }

    public void visitFieldInsn(int opcode, String name, String desc) {
        super.visitFieldInsn(opcode, className, name, desc);
    }

    private int getLocal(String assocName) {
        Integer var = locals.get(assocName);
        if (var == null) {
            throw new IllegalArgumentException("Local with assoc name '" + assocName + "' was never created!");
        }
        return var;
    }

    // for SOME REASON just calling super with the created locals does not work and
    // I have to keep a reference to the original MethodWriter
    public void visitVarInsn(int opcode, String assocName) {
        root.visitVarInsn(opcode, getLocal(assocName));
    }

    public void visitIincInsn(String assocName, int increment) {
        root.visitIincInsn(getLocal(assocName), increment);
    }

    public void ifJump(int opcode, Runnable body) {
        Label skip = new Label();
        visitJumpInsn(opcode, skip);
        body.run();
        visitLabel(skip);
    }

    public void ifJump(int opcode, Runnable body, Runnable or) {
        Label then = new Label();
        Label skip = new Label();
        visitJumpInsn(opcode, then);
        body.run();
        visitJumpInsn(GOTO, skip);
        visitLabel(then);
        or.run();
        visitLabel(skip);
    }
}
