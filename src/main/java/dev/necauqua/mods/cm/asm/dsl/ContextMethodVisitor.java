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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.GOTO;

public class ContextMethodVisitor extends MethodVisitor {

    private final String className;

    private int pass = 1; // used in `code` lambdas here and there
    private final int[] currentLineNumber; // used for debug

    public ContextMethodVisitor(String className, MethodVisitor parent) {
        super(Opcodes.ASM5, parent);
        this.className = className.replace('.', '/');
        this.currentLineNumber = new int[]{0};
    }

    public ContextMethodVisitor(ContextMethodVisitor mv) {
        super(Opcodes.ASM5, mv);
        className = mv.className;
        currentLineNumber = mv.currentLineNumber;
    }

    public void setCurrentLineNumber(int currentLineNumber) {
        this.currentLineNumber[0] = currentLineNumber;
    }

    public int getCurrentLineNumber() {
        return currentLineNumber[0];
    }

    public void setPass(int pass) {
        this.pass = pass;
    }

    public int getPass() {
        return pass;
    }

    public String getClassName() {
        return className;
    }

    // Additional methods:

    public void visitHook(Hook hook) {
        hook.accept(this);
    }

    public void visitFieldInsn(int opcode, String name, String desc) {
        visitFieldInsn(opcode, className, name, desc);
    }

    public void visitMethodInsn(int opcode, String name, String desc) {
        visitMethodInsn(opcode, className, name, desc, false);
    }

    public void ifJump(int opcode, Runnable skippedIfTrue) {
        Label skip = new Label();
        visitJumpInsn(opcode, skip);
        skippedIfTrue.run();
        visitLabel(skip);
    }

    public void ifJump(int opcode, Runnable skippedIfTrue, Runnable calledIfTrue) {
        Label then = new Label();
        Label skip = new Label();
        visitJumpInsn(opcode, then);
        skippedIfTrue.run();
        visitJumpInsn(GOTO, skip);
        visitLabel(then);
        calledIfTrue.run();
        visitLabel(skip);
    }
}
