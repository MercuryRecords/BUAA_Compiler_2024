package middleEnd;

public class LLVMConst extends LLVMExp implements UsableValue {

    private LLVMType.TypeID baseType;
    public int constValue;

    public LLVMConst(LLVMType.TypeID baseType, int constValue) {
        super();
        this.baseType = baseType;
        this.constValue = constValue;
        this.value = this;
    }

    @Override
    public String toValueIR() {
        return String.valueOf(constValue);
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
    public void setRegNo(int regNo) {
        throw new RuntimeException("Const can't have regNo");
    }

    public LLVMConst binaryOperate(LLVMType.InstType instType, LLVMConst right) {
        LLVMConst left = this;
        switch (instType) {
            case MUL  -> constValue *= right.constValue;
            case SDIV -> constValue /= right.constValue;
            case SREM -> constValue %= right.constValue;
            case ADD  -> constValue += right.constValue;
            case SUB  -> constValue -= right.constValue;
            case ICMP_SLT -> {
                constValue = left.constValue < right.constValue ? 1 : 0;
                baseType = LLVMType.TypeID.I1;
            }
            case ICMP_SLE -> {
                constValue = left.constValue <= right.constValue ? 1 : 0;
                baseType = LLVMType.TypeID.I1;
            }
            case ICMP_SGT -> {
                constValue = left.constValue > right.constValue ? 1 : 0;
                baseType = LLVMType.TypeID.I1;
            }
            case ICMP_SGE -> {
                constValue = left.constValue >= right.constValue ? 1 : 0;
                baseType = LLVMType.TypeID.I1;
            }
        }
        return left;
    }

    public LLVMConst negate() {
        constValue = -constValue;
        return this;
    }

    public LLVMConst logicalNot() {
        if (constValue == 0) {
            constValue = 1;
        } else {
            constValue = 0;
        }
        return this;
    }

    public void changeType(LLVMType.TypeID type) {
        baseType = type;
    }
}
