package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;

public class AllocaInst extends Instruction {
    private final int regNo;
    private final LLVMType.TypeID type;
    public AllocaInst(int regNo, LLVMType.TypeID baseType) {
        super(LLVMType.InstType.AllocaInst);
        this.regNo = regNo;
        this.type = baseType;
    }

    @Override
    public String toString() {
        return String.format("%%%d = alloca %s, align %s", regNo, type.toString(), type.toAlign());
    }
}
