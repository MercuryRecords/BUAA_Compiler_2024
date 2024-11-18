package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class BinaryInst extends Instruction implements UsableValue {
    protected int regNo;
    protected LLVMType.TypeID baseType;
    protected UsableValue op1;
    protected UsableValue op2;
    public BinaryInst(LLVMType.InstType type, int regNo, UsableValue op1, UsableValue op2) {
        super(type);
        this.regNo = regNo;
        this.op1 = op1;
        this.op2 = op2;
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
        return String.format("%s = %s %s %s, %s", toValueIR(), type.toString(), toLLVMType(), op1.toValueIR(), op2.toValueIR());
    }
}
