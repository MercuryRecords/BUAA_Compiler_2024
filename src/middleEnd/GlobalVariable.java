package middleEnd;

import frontEnd.Symbol;

public class GlobalVariable extends LLVMVariable {
    public GlobalVariable(Symbol symbol, int arrayLength) {
        super(symbol, arrayLength);
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
