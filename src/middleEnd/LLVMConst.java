package middleEnd;

public class LLVMConst extends Value implements ValueRepresentation {

    private final LLVMType.TypeID baseType;
    private final int val;

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
}
