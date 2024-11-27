package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class GetelementptrInst extends Instruction implements UsableValue {
    private int regNo;
    private final LLVMType.TypeID baseType;
    private final UsableValue from;
    private final UsableValue offset;
    public GetelementptrInst(LLVMType.TypeID baseType, UsableValue from, UsableValue offset) {
        super(LLVMType.InstType.GETELEMENTPTR);
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
        int tmpLen = from.toLLVMType().length() - 1;
        return String.format("%s = getelementptr %s, %s %s, i32 0, i32 %s", toValueIR(), from.toLLVMType().substring(0, tmpLen), from.toLLVMType(), from.toValueIR(), offset.toValueIR());
    }
}
