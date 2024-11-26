package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMLabel;
import middleEnd.UsableValue;

import static middleEnd.LLVMType.InstType.BRANCH;

public class BranchInst extends Instruction {
    private UsableValue val = null;
    private LLVMLabel isTrue = null;
    private LLVMLabel isFalse = null;
    public LLVMLabel dest = null;

    public BranchInst(LLVMLabel dest) {
        super(BRANCH);
        this.dest = dest;
    }

    public BranchInst(UsableValue val, LLVMLabel isTrue, LLVMLabel isFalse) {
        super(BRANCH);
        this.isTrue = isTrue;
        this.isFalse = isFalse;
        this.val = val;
    }

    @Override
    public String toString() {
        if (val == null) {
            return "br label " + dest.toValueIR();
        } else {
            return "br i1 " + val.toValueIR() + ", label " + isTrue.toValueIR() + ", label " + isFalse.toValueIR();
        }
    }
}
