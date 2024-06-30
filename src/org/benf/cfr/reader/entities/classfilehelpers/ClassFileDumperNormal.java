package org.benf.cfr.reader.entities.classfilehelpers;

import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.entities.*;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

import java.util.List;

public class ClassFileDumperNormal extends AbstractClassFileDumper {

    private static final AccessFlag[] dumpableAccessFlagsClass = new AccessFlag[]{
            AccessFlag.ACC_PUBLIC, AccessFlag.ACC_PRIVATE, AccessFlag.ACC_PROTECTED, AccessFlag.ACC_STRICT, AccessFlag.ACC_STATIC, AccessFlag.ACC_FINAL, AccessFlag.ACC_ABSTRACT, AccessFlag.ACC_FAKE_SEALED, AccessFlag.ACC_FAKE_NON_SEALED
    };
    private static final AccessFlag[] dumpableAccessFlagsInlineClass = new AccessFlag[]{
            AccessFlag.ACC_PUBLIC, AccessFlag.ACC_PRIVATE, AccessFlag.ACC_PROTECTED, AccessFlag.ACC_STRICT, AccessFlag.ACC_FINAL, AccessFlag.ACC_ABSTRACT, AccessFlag.ACC_FAKE_SEALED, AccessFlag.ACC_FAKE_NON_SEALED
    };

    public ClassFileDumperNormal(DCCommonState dcCommonState) {
        super(dcCommonState);
    }

    private void dumpHeader(ClassFile c, InnerClassDumpType innerClassDumpType, Dumper d) {
        AccessFlag[] accessFlagsToDump = innerClassDumpType == InnerClassDumpType.INLINE_CLASS ? dumpableAccessFlagsInlineClass : dumpableAccessFlagsClass;
        d.keyword(getAccessFlagsString(c.getAccessFlags(), accessFlagsToDump));

        d.keyword("class ");
        c.dumpClassIdentity(d);

        ClassSignature signature = c.getClassSignature();
        JavaTypeInstance superClass = signature.getSuperClass();
        if (superClass != null) {
            if (!superClass.getRawName().equals(TypeConstants.objectName)) {
                d.print(" ");
                d.keyword("extends ").dump(superClass).newln();
            }
        }

        dumpImplements(d, signature);
        dumpPermitted(c, d);
        d.removePendingCarriageReturn().print(" ");
    }

    @Override
    public Dumper dump(ClassFile classFile, InnerClassDumpType innerClass, Dumper d) {
        if (!d.canEmitClass(classFile.getClassType())) return d;

        if (!innerClass.isInnerClass()) {
            dumpTopHeader(classFile, d, true);
            dumpImports(d, classFile);
        }

        dumpComments(classFile, d);
        dumpAnnotations(classFile, d);
        dumpHeader(classFile, innerClass, d);
        d.separator("{").newln();
        d.indent(1);
        boolean first = true;

        List<ClassFileField> fields = classFile.getFields();
        for (ClassFileField field : fields) {
            if (!field.shouldNotDisplay()) {
                field.dump(d, classFile);
                first = false;
            }
        }
        dumpMethods(classFile, d, first, true);
        classFile.dumpNamedInnerClasses(d);
        d.indent(-1);
        d.separator("}").newln();

        return d;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }
}
