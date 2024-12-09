package middleEnd.Insts;

import backEnd.Insts.*;
import backEnd.MIPSComment;
import backEnd.MIPSInst;
import backEnd.MIPSManager;
import backEnd.Register;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class BinaryInst extends LLVMInstruction implements UsableValue {
    protected int regNo;
    protected LLVMType.TypeID baseType;
    protected UsableValue op1;
    protected UsableValue op2;
    public BinaryInst(LLVMType.InstType type, UsableValue op1, UsableValue op2) {
        super(type);
        this.op1 = op1;
        this.op2 = op2;
        switch (type) {
            case ICMP_EQ, ICMP_NE, ICMP_SGT, ICMP_SGE, ICMP_SLT, ICMP_SLE -> baseType = LLVMType.TypeID.I1;
            default -> baseType = LLVMType.TypeID.IntegerTyID;
        }
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
    public int getMemorySize() {
        return 4;
    }

    @Override
    public String toString() {
        return String.format("%s = %s %s %s, %s", toValueIR(), type.toString(), op1.toLLVMType(), op1.toValueIR(), op2.toValueIR());
    }

    @Override
    public LinkedList<MIPSInst> toMIPS() {
        LinkedList<MIPSInst> mipsInsts = new LinkedList<>();
        mipsInsts.add(new MIPSComment(this.toString()));

        Register reg1;
        if (op1.toValueIR().startsWith("%")) {
//            if (!MIPSManager.getInstance().hasReg(op1)) {
//                mipsInsts.addAll(MIPSManager.getInstance().deallocateReg());
//            }
            reg1 = MIPSManager.getInstance().getReg(op1);
//            MIPSManager.getInstance().reserveUsedReg(reg1);
            mipsInsts.add(MIPSManager.getInstance().loadValueToReg(op1, reg1));
        } else {
            // 是常数，加载到 K0 寄存器
            reg1 = Register.K0;
            mipsInsts.add(new LIInst(reg1, op1.toValueIR()));
        }

        Register reg2;
        if (op2.toValueIR().startsWith("%")) {
//            if (!MIPSManager.getInstance().hasReg(op2)) {
//                mipsInsts.addAll(MIPSManager.getInstance().deallocateReg());
//            }
            reg2 = MIPSManager.getInstance().getReg(op2);
//            MIPSManager.getInstance().reserveUsedReg(reg2);
            mipsInsts.add(MIPSManager.getInstance().loadValueToReg(op2, reg2));
        } else {
            // 是常数，加载到 K1 寄存器
            reg2 = Register.K1;
            mipsInsts.add(new LIInst(reg2, op2.toValueIR()));
        }

//        if (!MIPSManager.getInstance().hasReg(this)) {
//            mipsInsts.addAll(MIPSManager.getInstance().deallocateReg());
//        }
        Register reg = MIPSManager.getInstance().getReg(this);
        switch (type) {
            case ADD -> mipsInsts.add(new ADDUInst(reg1, reg2, reg));
            case SUB -> mipsInsts.add(new SUBUInst(reg1, reg2, reg));
            case MUL -> {
                mipsInsts.add(new MULTInst(reg1, reg2));
                mipsInsts.add(new MFLOInst(reg));
            }
            case SDIV ->{
                mipsInsts.add(new DIVInst(reg1, reg2));
                mipsInsts.add(new MFLOInst(reg));
            }
            case SREM ->{
                mipsInsts.add(new DIVInst(reg1, reg2));
                mipsInsts.add(new MFHIInst(reg));
            }
            case ICMP_EQ -> {
                mipsInsts.add(new SEQInst(reg, reg1, reg2));
            }
            case ICMP_NE -> {
                mipsInsts.add(new SNEInst(reg, reg1, reg2));
            }
            case ICMP_SGT -> {
                mipsInsts.add(new SGTInst(reg, reg1, reg2));
            }
            case ICMP_SGE -> {
                mipsInsts.add(new SGEInst(reg, reg1, reg2));
            }
            case ICMP_SLT -> {
                mipsInsts.add(new SLTInst(reg, reg1, reg2));
            }
            case ICMP_SLE -> {
                mipsInsts.add(new SLEInst(reg, reg1, reg2));
            }
        }
        mipsInsts.add(MIPSManager.getInstance().saveRegToMemory(this, reg));
        MIPSManager.getInstance().releaseRegs();

        return mipsInsts;
    }
}
