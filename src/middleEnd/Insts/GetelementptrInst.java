package middleEnd.Insts;

import backEnd.MIPSComment;
import backEnd.MIPSInst;
import middleEnd.LLVMInstruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class GetelementptrInst extends LLVMInstruction implements UsableValue {
    private int regNo;
    private final LLVMType.TypeID baseType;
    private final UsableValue from;
    private final UsableValue offset;
    public GetelementptrInst(LLVMType.TypeID baseType, UsableValue from, UsableValue offset) {
        super(LLVMType.InstType.GETELEMENTPTR);
        this.baseType = baseType.toPointerType();
        this.from = from;
        this.offset = offset;
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
        int tmpLen = from.toLLVMType().length() - 1;
        if (from.toLLVMType().charAt(0) == '[') {
            return String.format("%s = getelementptr %s, %s %s, i32 0, i32 %s", toValueIR(), from.toLLVMType().substring(0, tmpLen), from.toLLVMType(), from.toValueIR(), offset.toValueIR());
        } else {
            return String.format("%s = getelementptr %s, %s %s, i32 %s", toValueIR(), from.toLLVMType().substring(0, tmpLen), from.toLLVMType(), from.toValueIR(), offset.toValueIR());
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
