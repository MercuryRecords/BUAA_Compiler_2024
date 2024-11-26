package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class BinaryInst extends Instruction implements UsableValue {
    protected int regNo;
    protected LLVMType.TypeID baseType;
    protected UsableValue op1;
    protected UsableValue op2;
    public BinaryInst(LLVMType.InstType type, UsableValue op1, UsableValue op2) {
        super(type);
        this.op1 = op1;
        this.op2 = op2;
        switch (type) {
            case ICMP_EQ, ICMP_NE, ICMP_SGT, ICMP_SGE, ICMP_SLT, ICMP_SLE -> baseType = LLVMType.TypeID.I1;
            default -> baseType = LLVMType.TypeID.IntegerTyID;
        }
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
        return String.format("%s = %s %s %s, %s", toValueIR(), type.toString(), op1.toLLVMType(), op1.toValueIR(), op2.toValueIR());
    }
}
