package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;
import middleEnd.utils.RegTracker;

public class TruncInst extends Instruction implements UsableValue {
    private int regNo;
    private final LLVMType.TypeID baseType;
    private final UsableValue from;

    public TruncInst(RegTracker regTracker, UsableValue from, LLVMType.TypeID baseType) {
        super(LLVMType.InstType.TRUNC);
        regTracker.addValue(this);
        this.baseType = baseType;
        this.from = from;
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
        return String.format("%s = trunc %s %s to %s", toValueIR(), from.toLLVMType(), from.toValueIR(), toLLVMType());
    }
}
