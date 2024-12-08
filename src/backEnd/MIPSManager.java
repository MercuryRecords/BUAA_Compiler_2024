package backEnd;

import backEnd.Insts.LWInst;
import backEnd.Insts.SWInst;
import middleEnd.FuncFParam;
import middleEnd.LLVMFunction;
import middleEnd.LLVMLabel;
import middleEnd.UsableValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MIPSManager {
    private static final MIPSManager MIPS_MANAGER = new MIPSManager();
    private int offset = 0;
    private LLVMFunction currentFunction;
    private final HashMap<LLVMFunction, HashMap<UsableValue, Integer>> offsetMap = new HashMap<>();
    private final LinkedList<Register> allTempRegs = new LinkedList<>();
    private LinkedList<Register> used = new LinkedList<>();
    private LinkedList<Register> free = new LinkedList<>();
    private LinkedList<Register> usedArgs = new LinkedList<>();
    private LinkedList<Register> freeArgs = new LinkedList<>();
    private HashMap<Register, UsableValue> regMap = new HashMap<>();
    private HashSet<Register> reserved = new HashSet<>();
    private UsableValue forSP = new LLVMLabel();
    private UsableValue forRA = new LLVMLabel();
    private MIPSManager() {
        allTempRegs.add(Register.T0);
        allTempRegs.add(Register.T1);
        allTempRegs.add(Register.T2);
        allTempRegs.add(Register.T3);
        allTempRegs.add(Register.T4);
        allTempRegs.add(Register.T5);
        allTempRegs.add(Register.T6);
        allTempRegs.add(Register.T7);
        allTempRegs.add(Register.T8);
        allTempRegs.add(Register.T9);
        allTempRegs.add(Register.S0);
        allTempRegs.add(Register.S1);
        allTempRegs.add(Register.S2);
        allTempRegs.add(Register.S3);
        allTempRegs.add(Register.S4);
        allTempRegs.add(Register.S5);
        allTempRegs.add(Register.S6);
        allTempRegs.add(Register.S7);
    }

    public static MIPSManager getInstance() {
        return MIPS_MANAGER;
    }

    public int getOffset() {
        return offset;
    }

    public int getValueOffset(UsableValue value) {
        return offsetMap.get(currentFunction).get(value);
    }

    public void subOffset(int size) {
        offset -= size;
    }

    public void setCurrentFunction(LLVMFunction function) {
        currentFunction = function;
        offsetMap.putIfAbsent(function, new HashMap<>());
        free = new LinkedList<>(allTempRegs);
        used = new LinkedList<>();
        regMap = new HashMap<>();
        offset = 0;
    }

    public int allocateMemForAlloca(int size) {
        int offset = this.offset;
        subOffset(size);
        return offset;
//        HashMap<UsableValue, Integer> map = offsetMap.get(currentFunction);
//        int size = value.getMemorySize();
//        // 申请内存
//        if (!map.containsKey(value) && size != 0) {
//            map.put(value, offset);
//            subOffset(size);
//        }
    }

    public LinkedList<MIPSInst> deallocateReg() {
        // 如果没有空闲寄存器，就踢出一个占用寄存器，并将其值保存到占用该寄存器对应临时寄存器的内存对应位置
        LinkedList<MIPSInst> insts = new LinkedList<>();
        if (free.isEmpty()) {
            Register reg = nextUsedReg();
            used.remove(reg);
            free.add(reg);
            UsableValue value = regMap.get(reg);
            insts.add(new MIPSComment("saving " + value.toValueIR() + " to memory, reg: " + reg));
            if (offsetMap.get(currentFunction).containsKey(value)) {
                int offset = offsetMap.get(currentFunction).get(value);
                insts.add(new SWInst(Register.SP, reg, offset));
            } else {
                HashMap<UsableValue, Integer> map = offsetMap.get(currentFunction);
                int size = value.getMemorySize();
                // 申请内存
                map.put(value, offset);
                insts.add(new SWInst(Register.SP, reg, offset));
                subOffset(size);
            }
        }
        return insts;
    }

    public boolean hasReg(UsableValue value) {
        return regMap.containsValue(value);
    }

    public Register getReg(UsableValue value) {
        for (HashMap.Entry<Register, UsableValue> entry : regMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        Register reg = nextFreeReg();
        regMap.put(reg, value);
        free.remove(reg);
        used.add(reg);
        return reg;
    }

    private Register nextFreeReg() {
        return free.getFirst();
    }

    private Register nextUsedReg() {
        for (Register reg : used) {
            if (!reserved.contains(reg)) {
                return reg;
            }
        }
        throw new RuntimeException("No free register");
    }

    public void reserveUsedReg(Register fromReg) {
        reserved.add(fromReg);
    }

    public void resetReservedRegs() {
        reserved.clear();
    }

    public boolean hasFreeArgReg() {
        return !this.freeArgs.isEmpty();
    }

    public void setRegMap(Register reg, FuncFParam param) {
        regMap.put(reg, param);
    }

    public void setParamOffset(FuncFParam param, int offset) {
        offsetMap.get(currentFunction).put(param, offset);
    }

    public void allocateMemForArg() {
        offset -= 4;
    }

    public LinkedList<MIPSInst> storeAllReg() {
        LinkedList<MIPSInst> insts = new LinkedList<>();
        for (Register reg : used) {
            UsableValue value = regMap.get(reg);
            if (offsetMap.get(currentFunction).containsKey(value)) {
                int offset = offsetMap.get(currentFunction).get(value);
                insts.add(new SWInst(Register.SP, reg, offset));
            } else {
                HashMap<UsableValue, Integer> map = offsetMap.get(currentFunction);
                int size = value.getMemorySize();
                // 申请内存
                map.put(value, offset);
                insts.add(new SWInst(Register.SP, reg, offset));
                subOffset(size);
            }
        }
        HashMap<UsableValue, Integer> map = offsetMap.get(currentFunction);
        map.put(MIPS_MANAGER.forSP, offset);
        insts.add(new SWInst(Register.SP, Register.SP, offset));
        subOffset(4);
        map.put(MIPS_MANAGER.forRA, offset);
        insts.add(new SWInst(Register.SP, Register.RA, offset));
        subOffset(4);
        return insts;
    }

    public LinkedList<MIPSInst> restoreAllReg() {
        LinkedList<MIPSInst> insts = new LinkedList<>();
        for (Register reg : used) {
            UsableValue value = regMap.get(reg);
            int offset = offsetMap.get(currentFunction).get(value);
            insts.add(new LWInst(Register.SP, reg, offset));
        }
        HashMap<UsableValue, Integer> map = offsetMap.get(currentFunction);
        insts.add(new LWInst(Register.SP, Register.SP, map.get(MIPS_MANAGER.forSP)));
        insts.add(new LWInst(Register.SP, Register.RA, map.get(MIPS_MANAGER.forRA)));
        return insts;
    }

    public String getMIPSLabel(LLVMLabel dest) {
        return currentFunction.labelToBlock.get(dest).name;
    }
}