package middleEnd.Insts;

import backEnd.MIPSComment;
import backEnd.MIPSInst;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMLabel;
import middleEnd.UsableValue;

import java.util.LinkedList;

import static middleEnd.LLVMType.InstType.BRANCH;

public class BranchInst extends LLVMInstruction {
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

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        LinkedList<MIPSInst> mipsInsts = new LinkedList<>();
        mipsInsts.add(new MIPSComment(this.toString()));

        // TODO

        return mipsInsts;
    }
}
