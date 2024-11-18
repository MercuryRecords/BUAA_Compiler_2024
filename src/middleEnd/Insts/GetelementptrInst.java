package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.ValueRepresentation;

public class GetelementptrInst extends Instruction implements ValueRepresentation {
    private final int regNo;
    private final LLVMType.TypeID baseType;
    private final ValueRepresentation from;
    private final int offset;
    public GetelementptrInst(int regNo, LLVMType.TypeID baseType, ValueRepresentation from, int offset) {
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
        return String.format("%s = getelementptr %s, %s* %s, i32 0, i32 %d", toValueIR(), from.toLLVMType(), from.toLLVMType(), from.toValueIR(), offset);
    }
}
