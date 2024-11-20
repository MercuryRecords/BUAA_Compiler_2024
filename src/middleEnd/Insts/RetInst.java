package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class RetInst extends Instruction {
    UsableValue ret = null;
    public RetInst() {
        super(LLVMType.InstType.RETURN);
    }

    public RetInst(UsableValue value) {
        super(LLVMType.InstType.RETURN);
        ret = value;
    }

    @Override
    public String toString() {
        if (ret == null) {
            return "ret void";
        } else {
            return String.format("ret %s %s", ret.toLLVMType(), ret.toValueIR());
        }
    }
}
