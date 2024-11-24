package middleEnd;

public class LLVMConst extends LLVMExp implements UsableValue {

    private final LLVMType.TypeID baseType;
    public final int val;

    public LLVMConst(LLVMType.TypeID baseType, int val) {
        super();
        this.baseType = baseType;
        this.val = val;
    }

    @Override
    public String toValueIR() {
        return String.valueOf(val);
    }

    @Override
    public String toLLVMType() {
        return baseType.toString();
    }

    @Override
    public int toAlign() {
        return baseType.toAlign();
    }

    public LLVMConst binaryOperate(LLVMType.InstType instType, LLVMConst right) {
        LLVMConst left = this;
        switch (instType) {
            // TODO
        }
        return null;
    }

    public LLVMConst negate() {
        return new LLVMConst(baseType, -val);
    }

    public LLVMConst logicalNot() {
        return new LLVMConst(baseType, val == 0 ? 1 : 0);
    }
}
