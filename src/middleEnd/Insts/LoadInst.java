package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;
import middleEnd.utils.RegTracker;

public class LoadInst extends Instruction implements UsableValue {
    int regNo;
    LLVMType.TypeID baseType;
    UsableValue from;
    public LoadInst(LLVMType.TypeID baseType, UsableValue from) {
        super(LLVMType.InstType.LOAD);
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
        return String.format("%s = load %s, %s* %s, align %d", toValueIR(), toLLVMType(), toLLVMType(), from.toValueIR(), toAlign());
    }
}
