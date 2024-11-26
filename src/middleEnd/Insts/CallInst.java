package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

import java.util.LinkedList;

public class CallInst extends Instruction implements UsableValue {
    private final LLVMType.TypeID retType;
    private final String funcName;
    private final LinkedList<UsableValue> params;
    private int regNo;

    public CallInst(LLVMType.TypeID retType, String func, LinkedList<UsableValue> params) {
        super(LLVMType.InstType.CALL);
        this.retType = retType;
        this.funcName = func;
        this.params = params;
    }

    public CallInst(LLVMType.TypeID retType, String func) {
        super(LLVMType.InstType.CALL);
        this.retType = retType;
        this.funcName = func;
        this.params = new LinkedList<>();
    }

    public CallInst(String funcName, LinkedList<UsableValue> params) {
        super(LLVMType.InstType.CALL);
        this.retType = LLVMType.TypeID.VoidTyID;
        this.funcName = funcName;
        this.params = params;
    }

    @Override
    public String toValueIR() {
        return String.format("%%%d", regNo);
    }

    @Override
    public String toLLVMType() {
        return retType.toString();
    }

    @Override
    public int toAlign() {
        return retType.toAlign();
    }

    @Override
    public void setRegNo(int regNo) {
        this.regNo = regNo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.retType != LLVMType.TypeID.VoidTyID) {
            sb.append(String.format("%%%d = ", regNo));
        }
        sb.append(String.format("call %s @%s", toLLVMType(), funcName));
        sb.append("(");
        for (int i = 0; i < params.size(); i++) {
            UsableValue param = params.get(i);
            sb.append(param.toLLVMType());
            sb.append(" ");
            sb.append(param.toValueIR());
            if (i != params.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean isDifferentType(UsableValue val) {
        if (retType != LLVMType.TypeID.CharTyID && retType != LLVMType.TypeID.IntegerTyID) {
            return false;
        }

        return !val.toLLVMType().equals(retType.toPointerType().toString());
    }

    public Instruction fix() {
        if (retType == LLVMType.TypeID.CharTyID) {
            return new ZextInst(this, LLVMType.TypeID.IntegerTyID);
        } else {
            return new TruncInst(this, LLVMType.TypeID.CharTyID);
        }
    }

    public boolean isVoid() {
        return this.retType == LLVMType.TypeID.VoidTyID;
    }
}
