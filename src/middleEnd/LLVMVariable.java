package middleEnd;

import frontEnd.Symbol;

public class LLVMVariable extends Value {
    public boolean isConst;
    public String name;
    public int arrayLength; // 为 0 是表示不是数组
    public LLVMType.TypeID baseType;
    public InitVal initVal;

    public LLVMVariable(Symbol symbol, int arrayLength) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = arrayLength;
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }

    protected void setFromSymbol(Symbol symbol) {
        this.isConst = symbol.symbolType.toString().startsWith("Const");
        this.name = symbol.token.token;
        switch (symbol.symbolType) {
            case ConstInt:
            case Int:
            case IntArray:
            case ConstIntArray:
                this.baseType = LLVMType.TypeID.IntegerTyID;
                break;
            case ConstChar:
            case Char:
            case CharArray:
            case ConstCharArray:
                this.baseType = LLVMType.TypeID.CharTyID;
                break;
            default:
                throw new RuntimeException("wrong symbol type for GlobalVariable: " + symbol.symbolType);
        }
    }
}
