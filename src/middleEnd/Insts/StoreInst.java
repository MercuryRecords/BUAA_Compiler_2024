package middleEnd.Insts;

import backEnd.MIPSComment;
import backEnd.MIPSInst;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class StoreInst extends LLVMInstruction {

    private final UsableValue from;
    private final UsableValue to;

    public StoreInst(UsableValue from, UsableValue to) {
        super(LLVMType.InstType.STORE);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return String.format("store %s %s, %s %s", from.toLLVMType(), from.toValueIR(), to.toLLVMType(), to.toValueIR());
    }

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        LinkedList<MIPSInst> mipsInsts = new LinkedList<>();
        mipsInsts.add(new MIPSComment(this.toString()));

        // TODO

        return mipsInsts;
    }
}
