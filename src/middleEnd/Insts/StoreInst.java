package middleEnd.Insts;

import backEnd.Insts.*;
import backEnd.MIPSComment;
import backEnd.MIPSInst;
import backEnd.Register;
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
        /*
        # store i32 2, i32* %2
        li $t0, 2
        sw $t0, 4($fp)
         */
        if (from.toValueIR().startsWith("%") || from.toValueIR().startsWith("@")) {
            // 从内存中加载
            // TODO 全局变量
            if (from.toLLVMType().contains("i32")) {
                mipsInsts.add(new LWInst(Register.FP, Register.T0, from.offsetInMemory()));
            } else {
                mipsInsts.add(new LBInst(Register.FP, Register.T0, from.offsetInMemory()));
            }
        } else {
            mipsInsts.add(new LIInst(Register.T0, Integer.parseInt(from.toValueIR())));
        }

        if (to.toLLVMType().contains("i32")) {
            mipsInsts.add(new SWInst(Register.FP, Register.T0, to.offsetInMemory()));
        } else {
            mipsInsts.add(new SBInst(Register.FP, Register.T0, to.offsetInMemory()));
        }

        return mipsInsts;
    }
}
