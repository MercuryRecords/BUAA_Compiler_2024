package backEnd;

import middleEnd.UsableValue;

import java.util.HashSet;

public class Dispatcher {
    private static final Dispatcher dispatcher = new Dispatcher();
    private int offset = 0;
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

    public Register dispatchReg(UsableValue value) {
        // TODO 实现寄存器分配
    }
}