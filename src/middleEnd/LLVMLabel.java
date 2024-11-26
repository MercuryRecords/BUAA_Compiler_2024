package middleEnd;

import static middleEnd.LLVMType.InstType.LABEL;

public class LLVMLabel extends Instruction implements UsableValue {
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
    public String toString() {
        return String.format("\n%d: ", regNo);
    }
}
