package middleEnd;

import java.util.LinkedList;

public class Block extends Value {
    private final LinkedList<Instruction> insts = new LinkedList<>();

    public void addInst(LinkedList<Instruction> instructions) {
        insts.addAll(instructions);
    }

    @Override
    public String toString() {
        StringBuilder block = new StringBuilder();
        block.append("{\n");
        for (Instruction inst : insts) {
            block.append("\t").append(inst.toString()).append("\n");
        }
        block.append("\n}");
        return block.toString();
    }
}
