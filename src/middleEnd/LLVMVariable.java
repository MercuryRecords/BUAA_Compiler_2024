package middleEnd;

import frontEnd.Symbol;
import middleEnd.Insts.AllocaInst;
import middleEnd.Insts.GetelementptrInst;
import middleEnd.Insts.StoreInst;
import middleEnd.utils.RegTracker;

import java.util.LinkedList;

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

    public LinkedList<Instruction> getInstructions(RegTracker tracker) {
        LinkedList<Instruction> instructions = new LinkedList<>();
        if (arrayLength == 0) {
            // 单个变量/常量
            if (isConst) {
                // 单个常量
                int initValAsInt = ((ConstInitVal) initVal).getConstValue(0);
                AllocaInst allocaInst = new AllocaInst(tracker.nextRegNo(), baseType, arrayLength);
                instructions.add(allocaInst);
                LLVMConst llvmConst = new LLVMConst(baseType, initValAsInt);
                instructions.add(new StoreInst(llvmConst, allocaInst));
            } else {
                // 单个变量
                if (initVal != null) {

                } else {
                    AllocaInst allocaInst = new AllocaInst(tracker.nextRegNo(), baseType, arrayLength);
                    instructions.add(allocaInst);
                }
            }
        } else {
            // 数组
            if (isConst || initVal instanceof ConstInitVal) {
                // 常量数组
                ConstInitVal constInitVal = (ConstInitVal) initVal;
                AllocaInst allocaInst = new AllocaInst(tracker.nextRegNo(), baseType, arrayLength);
                instructions.add(allocaInst);
                for (int i = 0; i < arrayLength; i++) {
                    GetelementptrInst getelementptrInst = new GetelementptrInst(tracker.nextRegNo(), baseType, allocaInst, i);
                    instructions.add(getelementptrInst);
                    LLVMConst llvmConst = new LLVMConst(baseType, constInitVal.getConstValue(i));
                    instructions.add(new StoreInst(llvmConst, getelementptrInst));
                }
            } else {
                // 变量数组
                if (initVal != null) {

                } else {
                    AllocaInst allocaInst = new AllocaInst(tracker.nextRegNo(), baseType, arrayLength);
                    instructions.add(allocaInst);
                }
            }
        }
        return instructions;
    }
}
