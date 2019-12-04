package dev.necauqua.mods.cm.asm.dsl;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

public final class LineNumberReader extends MethodVisitor {

    private final ContextMethodVisitor context;

    public LineNumberReader(MethodVisitor parent, ContextMethodVisitor context) {
        super(ASM5, parent);
        this.context = context;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        context.setCurrentLineNumber(line);
    }
}
