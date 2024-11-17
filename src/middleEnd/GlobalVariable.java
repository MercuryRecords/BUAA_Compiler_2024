package middleEnd;

import frontEnd.Symbol;

public class GlobalVariable extends LLVMSymbol {
    // "全局变量的初值表达式也必须是常量表达式 ConstExp"，故都可以计算得出

    public GlobalVariable(Symbol symbol, int arrayLength) {
        super(symbol, arrayLength);
    }

    public int getConstValue(int i) {
        if (!isConst) {
            throw new RuntimeException("GlobalVariable is not const");
        }

        return ((ConstInitVal) initVal).getConstValue(i);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ConstInitVal constInitVal = (ConstInitVal) this.initVal;
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
                int val = constInitVal.getConstValue(i);
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
