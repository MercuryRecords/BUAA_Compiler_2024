package backEnd;

import java.util.LinkedList;

public class MIPSBasicBlock {
    public LinkedList<MIPSInst> instructions = new LinkedList<>();

    public void addInstructions(LinkedList<MIPSInst> instruction) {
        instructions.addAll(instruction);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MIPSInst inst : instructions) {
            sb.append("\t").append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}
