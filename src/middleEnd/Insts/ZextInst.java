package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class ZextInst extends Instruction implements UsableValue {
    private int regNo;
    private final LLVMType.TypeID baseType;
    private final UsableValue from;

    public ZextInst(UsableValue from) {
        super(LLVMType.InstType.ZEXT);
        this.baseType = LLVMType.TypeID.LongTyID;
        this.from = from;
    }

    public ZextInst(UsableValue from, LLVMType.TypeID baseType) {
        super(LLVMType.InstType.ZEXT);
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
        return String.format("%s = zext %s %s to %s", toValueIR(), from.toLLVMType(), from.toValueIR(), toLLVMType());
    }
}
