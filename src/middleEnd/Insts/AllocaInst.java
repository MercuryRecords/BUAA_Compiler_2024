package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.ValueRepresentation;

public class AllocaInst extends Instruction implements ValueRepresentation {
    private final int regNo;
    private final LLVMType.TypeID baseType;
    private final int arrayLength;
    public AllocaInst(int regNo, LLVMType.TypeID baseType, int arrayLength) {
        super(LLVMType.InstType.ALLOCA);
        this.regNo = regNo;
        this.baseType = baseType;
        this.arrayLength = arrayLength;
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
//        return String.format("%s = alloca %s, align %s", toValueIR(), type.toString(), type.toAlign());
        if (arrayLength == 0) {
            return String.format("%s = alloca %s, align %s", toValueIR(), baseType.toString(), baseType.toAlign());
        } else {
            return String.format("%s = alloca [%s x %s], align %s", toValueIR(), arrayLength, baseType.toString(), baseType.toAlign());
        }
    }
}
