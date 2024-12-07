package backEnd;

import middleEnd.Insts.AllocaInst;
import middleEnd.UsableValue;

import java.util.HashMap;
import java.util.HashSet;

public class Dispatcher {
    private static final Dispatcher dispatcher = new Dispatcher();
    private int offset = 0;
    private MIPSFunction currentFunction;
    private HashMap<MIPSFunction, HashMap<UsableValue, Integer>> offsetMap = new HashMap<>();
    private HashSet<Register> used = new HashSet<>();
    private HashSet<Register> free = new HashSet<>();
    private Dispatcher() {
    }

    public static Dispatcher getInstance() {
        return dispatcher;
    }

    public int getOffset() {
        return offset;
    }

    public void addOffset(int size) {
        offset += size;
    }

    public void dispatchReg(UsableValue value) {
        HashMap<UsableValue, Integer> map = offsetMap.get(currentFunction);
        int size = value.getMemorySize();
        if (!map.containsKey(value) && size != 0) {
            map.put(value, offset);
            addOffset(size);
        }



    }
}