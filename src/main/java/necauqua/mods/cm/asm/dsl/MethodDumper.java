package necauqua.mods.cm.asm.dsl;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.objectweb.asm.Opcodes.ASM5;

public final class MethodDumper extends MethodVisitor {

    private static final Path DUMP_DIR = Paths.get("./dumps");

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
            Files.createDirectories(DUMP_DIR);
            Path path = DUMP_DIR.resolve(className.replace('.', '_') + "#" + name + "-" + classifier + ".dump");
            PrintWriter writer = new PrintWriter(new FileOutputStream(path.toString()));
            writer.print('\n' + classifier.toUpperCase() + " DUMP OF METHOD " + name + '\n');
            tmv.p.print(writer);
            writer.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
