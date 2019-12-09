package dev.necauqua.mods.cm.asm.dsl;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

public final class DebugInfoReader extends MethodVisitor {

    private final ContextMethodVisitor context;
    private final ClassPatchVisitor cv;
    private final String method;

    public DebugInfoReader(MethodVisitor parent, ContextMethodVisitor context, ClassPatchVisitor cv, String method) {
        super(ASM5, parent);
        this.context = context;
        this.cv = cv;
        this.method = method;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        cv.setVisitingMethod(method);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        context.setCurrentLineNumber(line);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        cv.setVisitingMethod(null);
    }
}
