package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.ValueRepresentation;

public class GetelementptrInst extends Instruction implements ValueRepresentation {
    private final int regNo;
    private final LLVMType.TypeID baseType;
    private final ValueRepresentation target;
    private final int offset;
    public GetelementptrInst(int regNo, LLVMType.TypeID baseType, ValueRepresentation target, int offset) {
        super(LLVMType.InstType.GETELEMENTPTR);
        this.regNo = regNo;
        this.baseType = baseType.toPointerType();
        this.target = target;
        this.offset = offset;
    }

    @Override
    public String toValueIR() {
        return String.format("%%%d", regNo);
    }

    @Override
    public LLVMType.TypeID toLLVMType() {
        return baseType;
    }

    @Override
    public String toString() {
        return String.format("%s = getelementptr %s, %s %s, i32 %d", toValueIR(), target.toLLVMType(), baseType, target.toValueIR(), offset);
    }
}
