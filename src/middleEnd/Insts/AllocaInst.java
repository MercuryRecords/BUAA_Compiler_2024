package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.ValueRepresentation;

public class AllocaInst extends Instruction implements ValueRepresentation {
    private final int regNo;
    private final LLVMType.TypeID type;
    public AllocaInst(int regNo, LLVMType.TypeID baseType) {
        super(LLVMType.InstType.Alloca);
        this.regNo = regNo;
        this.type = baseType;
    }

    @Override
    public String toValueIR() {
        return String.format("%%%d", regNo);
    }

    @Override
    public LLVMType.TypeID toLLVMType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s, align %s", toValueIR(), type.toString(), type.toAlign());
    }
}
