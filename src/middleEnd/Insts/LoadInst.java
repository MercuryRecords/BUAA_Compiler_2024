package middleEnd.Insts;

import backEnd.Dispatcher;
import backEnd.Insts.LBInst;
import backEnd.Insts.LWInst;
import backEnd.Insts.LAInst;
import backEnd.MIPSComment;
import backEnd.MIPSInst;
import backEnd.Register;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class LoadInst extends LLVMInstruction implements UsableValue {
    int regNo;
    LLVMType.TypeID baseType;
    UsableValue from;
    boolean holdingRegister = false;
    Register reg;
    int offsetInMemory;
    public LoadInst(LLVMType.TypeID baseType, UsableValue from) {
        super(LLVMType.InstType.LOAD);
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
    public String toString() {
        return String.format("%s = load %s, %s* %s", toValueIR(), toLLVMType(), toLLVMType(), from.toValueIR());
    }

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        LinkedList<MIPSInst> mipsInsts = new LinkedList<>();
        mipsInsts.add(new MIPSComment(this.toString()));

        // TODO

        return mipsInsts;
    }
}
