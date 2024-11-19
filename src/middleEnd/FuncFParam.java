package middleEnd;

import frontEnd.Symbol;

public class FuncFParam extends Value implements UsableValue {
    int regNo;
    String name;
    LLVMType.TypeID baseType;
    public FuncFParam(int regNo, Symbol symbol) {
        this.regNo = regNo;
        name = symbol.token.token;
        switch (symbol.symbolType) {
            case Int:
                this.baseType = LLVMType.TypeID.IntegerTyID;
                break;
            case IntArray:
                this.baseType = LLVMType.TypeID.IntegerPtrTyID;
                break;
            case Char:
                this.baseType = LLVMType.TypeID.CharTyID;
                break;
            case CharArray:
                this.baseType = LLVMType.TypeID.CharPtrTyID;
                break;
            default:
                throw new RuntimeException("wrong symbol type for FuncFParam: " + symbol.symbolType);
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
    public String toString() {
        return String.format("%s %s", toLLVMType(), toValueIR());
    }
}
