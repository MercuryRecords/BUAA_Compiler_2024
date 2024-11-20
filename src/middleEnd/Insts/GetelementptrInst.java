package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class GetelementptrInst extends Instruction implements UsableValue {
    private final int regNo;
    private final LLVMType.TypeID baseType;
    private final UsableValue from;
    private final String offset;
    public GetelementptrInst(int regNo, LLVMType.TypeID baseType, UsableValue from, String offset) {
        super(LLVMType.InstType.GETELEMENTPTR);
        this.regNo = regNo;
        this.baseType = baseType;
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
        if (from.toLLVMType().endsWith("*")) {
            // %4 = getelementptr inbounds i8, i8* %3, i64 0
            return String.format("%s = getelementptr inbounds %s, %s %s, i64 %s", toValueIR(), from.toLLVMType().substring(0, from.toLLVMType().length() - 1), from.toLLVMType(), from.toValueIR(), offset);
        } else {
            return String.format("%s = getelementptr %s, %s* %s, i64 0, i64 %s", toValueIR(), from.toLLVMType(), from.toLLVMType(), from.toValueIR(), offset);
        }
    }
}
