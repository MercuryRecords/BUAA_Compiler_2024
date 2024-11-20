package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class GetelementptrInst extends Instruction implements UsableValue {
    private final int regNo;
    private final LLVMType.TypeID baseType;
    private final LLVMType.TypeID noPointerBaseType;
    private final UsableValue from;
    private final String offset;
    public GetelementptrInst(int regNo, LLVMType.TypeID baseType, UsableValue from, String offset) {
        super(LLVMType.InstType.GETELEMENTPTR);
        this.regNo = regNo;
        this.noPointerBaseType = baseType;
        this.baseType = baseType.toPointerType();
        this.from = from;
        this.offset = offset;
    }

    @Override
    public String toValueIR() {
        return String.format("%%%d", regNo);
    }

    @Override
    public String toLLVMType() {
        return baseType.toString();
    }

    @Override
    public int toAlign() {
        return baseType.toAlign();
    }

    @Override
    public String toString() {
        if (from.toLLVMType().startsWith("[")) {
            return String.format("%s = getelementptr %s, %s* %s, i64 0, i64 %s", toValueIR(), from.toLLVMType(), from.toLLVMType(), from.toValueIR(), offset);
        } else {
            return String.format("%s = getelementptr %s, %s %s, i64 %s", toValueIR(), noPointerBaseType.toString(), from.toLLVMType(), from.toValueIR(), offset);
        }
    }
}
