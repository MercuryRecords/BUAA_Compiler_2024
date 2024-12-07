package backEnd;

import backEnd.Insts.SWInst;
import middleEnd.UsableValue;

import java.util.HashMap;
import java.util.LinkedList;

public class MIPSManager {
    private static final MIPSManager MIPS_MANAGER = new MIPSManager();
    private int offset = 0;
    private MIPSFunction currentFunction;
    private final HashMap<MIPSFunction, HashMap<UsableValue, Integer>> offsetMap = new HashMap<>();
    private final LinkedList<Register> allTempRegs = new LinkedList<>();
    private LinkedList<Register> used = new LinkedList<>();
    private LinkedList<Register> free = new LinkedList<>();
    private HashMap<Register, UsableValue> regMap = new HashMap<>();
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

    public void addOffset(int size) {
        offset += size;
    }

    public void setCurrentFunction(MIPSFunction function) {
        currentFunction = function;
        offsetMap.putIfAbsent(function, new HashMap<>());
        free = new LinkedList<>(allTempRegs);
        used = new LinkedList<>();
    }

    public void allocateMem(UsableValue value) {
        HashMap<UsableValue, Integer> map = offsetMap.get(currentFunction);
        int size = value.getMemorySize();
        // 申请内存
        if (!map.containsKey(value) && size != 0) {
            map.put(value, offset);
            addOffset(size);
        }
    }

    public LinkedList<MIPSInst> deallocateReg() {
        // 如果没有空闲寄存器，就踢出一个占用寄存器，并将其值保存到占用该寄存器对应临时寄存器的内存对应位置
        LinkedList<MIPSInst> insts = new LinkedList<>();
        if (free.isEmpty()) {
            Register reg = nextUsedReg();
            used.remove(reg);
            free.add(reg);
            UsableValue value = regMap.get(reg);
            int offset = offsetMap.get(currentFunction).get(value);
            insts.add(new SWInst(Register.SP, reg, offset));
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
        return used.getFirst();
    }
}