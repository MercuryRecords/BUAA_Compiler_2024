package middleEnd;

import backEnd.MIPSInst;

import java.util.LinkedList;

import static middleEnd.LLVMType.InstType.LABEL;

public class LLVMLabel extends LLVMInstruction implements UsableValue {
    private int regNo;
    public LLVMLabel() {
        super(LABEL);
    }

    @Override
    public String toValueIR() {
        return String.format("%%%d", regNo);
    }

    @Override
    public String toLLVMType() {
        return "";
    }

    @Override
    public int toAlign() {
        return 0;
    }

    @Override
    public void setRegNo(int regNo) {
        this.regNo = regNo;
    }

    @Override
    public int offsetInMemory() {
        throw new RuntimeException("Label has no offset in memory");
    }

    @Override
    public String toString() {
        return String.format("\n%d: ", regNo);
    }

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        throw new RuntimeException("Label should not be translated to MIPS, maybe");
    }
}
