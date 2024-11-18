package middleEnd.Insts;

import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class SubInst extends BinaryInst {
    public SubInst(int regNo, UsableValue op1, UsableValue op2) {
        super(LLVMType.InstType.SUB, regNo, op1, op2);
    }
}
