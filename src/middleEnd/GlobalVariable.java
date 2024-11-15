package middleEnd;

import frontEnd.Symbol;

public class GlobalVariable extends GlobalValue {
    private boolean isConst;
    public String name;
    private final int arrayLength;
    private LLVMType.TypeID baseType;
    private InitVal initVal = null;
    public GlobalVariable(Symbol symbol, int arrayLength, InitVal initVal) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = arrayLength;
        this.initVal = initVal;
    }

    public GlobalVariable(Symbol symbol, InitVal initVal) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = 0;
        this.initVal = initVal;
    }

    public GlobalVariable(Symbol symbol, int arrayLength) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = arrayLength;
    }

    public GlobalVariable(Symbol symbol) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = 0;
    }

    private void setFromSymbol(Symbol symbol) {
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

    public int getConstValue() {
        return 0;
        // TODO
    }

    public int getConstValue(int i) {
        return 0;
        // TODO
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@").append(this.name).append(" = dso_local ");
        if (isConst) {
            sb.append("constant ");
        } else {
            sb.append("global ");
        }
        if (arrayLength > 0) {
            sb.append("[");
            sb.append(arrayLength);
            sb.append(" x ");
            sb.append(baseType);
            sb.append("] [");
            // TODO initVal
            sb.append("]");
        } else {
            sb.append(baseType);
            // TODO initVal
        }
        if (baseType == LLVMType.TypeID.IntegerTyID) {
            sb.append(", align 4");
        } else {
            sb.append(", align 1");
        }
        return sb.toString();
    }
}
