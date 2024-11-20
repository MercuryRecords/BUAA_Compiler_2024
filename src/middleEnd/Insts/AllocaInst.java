package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class AllocaInst extends Instruction implements UsableValue {
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
    public String toLLVMType() {
        if (arrayLength == 0) {
            return baseType.toPointerType().toString();
        } else {
            return String.format("[%d x %s]*", arrayLength, baseType.toString());
        }
    }

    @Override
    public int toAlign() {
        return baseType.toAlign();
    }

    private String toNoPointerType() {
        if (arrayLength == 0) {
            return baseType.toString();
        } else {
            return String.format("[%d x %s]", arrayLength, baseType.toString());
        }
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s, align %s", toValueIR(), toNoPointerType(), baseType.toAlign());
    }
}
