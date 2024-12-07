package middleEnd.Insts;

import backEnd.MIPSComment;
import backEnd.MIPSInst;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class TruncInst extends LLVMInstruction implements UsableValue {
    private int regNo;
    private final LLVMType.TypeID baseType;
    private final UsableValue from;

    public TruncInst(UsableValue from, LLVMType.TypeID baseType) {
        super(LLVMType.InstType.TRUNC);
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
    public void setVirtualRegNo(int regNo) {
        this.regNo = regNo;
    }

    @Override
    public int getMemorySize() {
        return baseType.toAlign();
    }

    @Override
    public String toString() {
        return String.format("%s = trunc %s %s to %s", toValueIR(), from.toLLVMType(), from.toValueIR(), toLLVMType());
    }

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        LinkedList<MIPSInst> mipsInsts = new LinkedList<>();
        mipsInsts.add(new MIPSComment(this.toString()));

        // TODO

        return mipsInsts;
    }
}
