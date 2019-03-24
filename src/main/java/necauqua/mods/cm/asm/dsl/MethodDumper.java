package necauqua.mods.cm.asm.dsl;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.ASM5;

public final class MethodDumper extends MethodVisitor {

    private final String classifier;
    private final String className;
    private final String name;
    private final TraceMethodVisitor tmv;

    private MethodDumper(String classifier, String className, String name, TraceMethodVisitor tmv) {
        super(ASM5, tmv);
        this.classifier = classifier;
        this.className = className;
        this.name = name;
        this.tmv = tmv;
    }

    public static MethodVisitor create(MethodVisitor parent, String header, String className, String name) {
        return new MethodDumper(header, className, name, new TraceMethodVisitor(parent, new Textifier()));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        try {
            String filename = "./dumps/" + className.replace('.', '_') + "#" + name + "-" + classifier + ".dump";
            PrintWriter writer = new PrintWriter(new FileOutputStream(filename));
            writer.print('\n' + classifier.toUpperCase() + " DUMP OF METHOD " + name + '\n');
            tmv.p.print(writer);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new AssertionError(e);
        }
    }
}
