package middleEnd;

import frontEnd.Symbol;

public class GlobalVariable extends GlobalValue {
    private boolean isConst;
    public String name;
    private final int arrayLength;
    private LLVMType.TypeID baseType;
    private ConstInitVal constInitVal = null;
    // "全局变量的初值表达式也必须是常量表达式 ConstExp"，故都可以计算得出
    public GlobalVariable(Symbol symbol, int arrayLength, ConstInitVal constInitVal) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = arrayLength;
        this.constInitVal = constInitVal;
    }

    public GlobalVariable(Symbol symbol, ConstInitVal constInitVal) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = 0;
        this.constInitVal = constInitVal;
    }

    public GlobalVariable(Symbol symbol, int arrayLength) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = arrayLength;
        this.constInitVal = new ConstInitVal(true, arrayLength);
    }

    public GlobalVariable(Symbol symbol) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = 0;
        this.constInitVal = new ConstInitVal(false, 0);
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
        if (isConst) {
            return constInitVal.getConstValue(0);
        }

        throw new RuntimeException("GlobalVariable is not const");
    }

    public int getConstValue(int i) {
        if (i >= arrayLength) {
            throw new RuntimeException("Overflow when getting const value from GlobalVariable");
        }

        if (!isConst) {
            throw new RuntimeException("GlobalVariable is not const");
        }

        if (i < constInitVal.getConstLength()) {
            return constInitVal.getConstValue(i);
        } else {
            return 0;
        }
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
            for (int i = 0; i < arrayLength; i++) {
                sb.append(baseType);
                sb.append(" ");
                int val;
                if (i < constInitVal.getConstLength()) {
                    val = constInitVal.getConstValue(i);
                } else {
                    val = 0;
                }
                if (baseType == LLVMType.TypeID.CharTyID) {
                    val = val & 0xFF;
                }
                sb.append(val);
                if (i < arrayLength - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        } else {
            sb.append(baseType);
            sb.append(" ");
            int val = constInitVal.getConstValue(0);
            if (baseType == LLVMType.TypeID.CharTyID) {
                val = val & 0xFF;
            }
            sb.append(val);
        }
        if (baseType == LLVMType.TypeID.IntegerTyID) {
            sb.append(", align 4");
        } else {
            sb.append(", align 1");
        }
        return sb.toString();
    }
}
