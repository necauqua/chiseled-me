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

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.GOTO;

// dont ever ask me how this class works with locals. Such hax
public class ContextMethodVisitor extends MethodVisitor {

    private final String className;
    private final Map<String, Integer> locals;
    private final LocalVariablesSorter lvs;
    private final MethodVisitor rootMV;

    private int pass = 1; // used in `code` lambdas here and there

    public ContextMethodVisitor(String className, List<Pair<String, Type>> locals, int access, String desc, MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
        this.className = className.replace('.', '/');
        this.locals = new HashMap<>();
        if (!locals.isEmpty()) {
            lvs = new LocalVariablesSorter(access, desc, mv);
            locals.forEach(p -> this.locals.put(p.getLeft(), lvs.newLocal(p.getRight())));
        } else {
            lvs = null;
        }
        rootMV = mv;
    }

    public ContextMethodVisitor(ContextMethodVisitor mv) {
        super(Opcodes.ASM5, mv);
        className = mv.className;
        locals = mv.locals;
        rootMV = mv.rootMV;
        lvs = null;
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

    public void visitVarInsn(int opcode, String assocName) {
        rootMV.visitVarInsn(opcode, getLocal(assocName));
    }

    public void visitIincInsn(String assocName, int increment) {
        rootMV.visitIincInsn(getLocal(assocName), increment);
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

    // Overrides for root LVS:

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (lvs != null) {
            lvs.visitVarInsn(opcode, var);
        } else {
            super.visitVarInsn(opcode, var);
        }
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        if (lvs != null) {
            lvs.visitIincInsn(var, increment);
        } else {
            super.visitIincInsn(var, increment);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (lvs != null) {
            lvs.visitMaxs(maxStack, maxLocals);
        } else {
            super.visitMaxs(maxStack, maxLocals);
        }
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if (lvs != null) {
            lvs.visitLocalVariable(name, desc, signature, start, end, index);
        } else {
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        if (lvs != null) {
            return lvs.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        } else {
            return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        }
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        if (lvs != null) {
            lvs.visitFrame(type, nLocal, local, nStack, stack);
        } else {
            super.visitFrame(type, nLocal, local, nStack, stack);
        }
    }
}
