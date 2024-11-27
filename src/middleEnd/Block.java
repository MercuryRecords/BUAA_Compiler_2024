package middleEnd;

import java.util.LinkedList;

public class Block extends Value {
    private final LinkedList<Instruction> insts = new LinkedList<>();

    public void addInsts(LinkedList<Instruction> instructions) {
        insts.addAll(instructions);
    }

    public void addInst(Instruction instruction) {
        insts.add(instruction);
    }

    @Override
    public String toString() {
        StringBuilder block = new StringBuilder();
        block.append("{\n");
        for (Instruction inst : insts) {
            block.append("\t").append(inst.toString()).append("\n");
        }
        block.append("}");
        return block.toString();
    }

    public LinkedList<Instruction> getInstructions() {
        return insts;
    }
}
