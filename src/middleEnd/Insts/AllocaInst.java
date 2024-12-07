package middleEnd.Insts;

import backEnd.MIPSComment;
import backEnd.MIPSInst;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class AllocaInst extends LLVMInstruction implements UsableValue {
    private int regNo;
    private final LLVMType.TypeID baseType;
    private final int arrayLength;
    public AllocaInst(LLVMType.TypeID baseType, int arrayLength) {
        super(LLVMType.InstType.ALLOCA);
        this.baseType = baseType;
        this.arrayLength = arrayLength;
    }

    public void setVirtualRegNo(int regNo) {
        this.regNo = regNo;
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

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        LinkedList<MIPSInst> mipsInsts = new LinkedList<>();
        mipsInsts.add(new MIPSComment(this.toString()));

        // TODO
//        int size = baseType.toAlign() * arrayLength;
//        mipsInsts.add(new ADDIUInst(Register.SP, Register.SP, -size));
//        offsetInMemory = Dispatcher.getInstance().getOffset();
//        Dispatcher.getInstance().addOffset(size);

        return mipsInsts;
    }
}
