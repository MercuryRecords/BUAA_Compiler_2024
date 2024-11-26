package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;
import middleEnd.utils.RegTracker;

public class GetelementptrInst extends Instruction implements UsableValue {
    private int regNo;
    private final LLVMType.TypeID baseType;
    private final LLVMType.TypeID noPointerBaseType;
    private final UsableValue from;
    private final String offset;
    public GetelementptrInst(LLVMType.TypeID baseType, UsableValue from, String offset) {
        super(LLVMType.InstType.GETELEMENTPTR);
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
    public void setRegNo(int regNo) {
        this.regNo = regNo;
    }

    @Override
    public String toString() {
        if (from.toLLVMType().startsWith("[")) {
            // 唉特判
            return String.format("%s = getelementptr %s, %s* %s, i64 0, i64 %s", toValueIR(), from.toLLVMType(), from.toLLVMType(), from.toValueIR(), offset);
        } else {
            return String.format("%s = getelementptr %s, %s %s, i64 %s", toValueIR(), noPointerBaseType.toString(), from.toLLVMType(), from.toValueIR(), offset);
        }
    }
}
