package middleEnd.Insts;

import backEnd.MIPSComment;
import backEnd.MIPSInst;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class RetInst extends LLVMInstruction {
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

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        LinkedList<MIPSInst> mipsInsts = new LinkedList<>();
        mipsInsts.add(new MIPSComment(this.toString()));

        // TODO

        return mipsInsts;
    }
}
