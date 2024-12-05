package backEnd;

import java.util.LinkedList;

public class MIPSTextSection {
    private final LinkedList<MIPSFunction> functions = new LinkedList<>();

    public void addFunction(MIPSFunction mipsFunction) {
        functions.add(mipsFunction);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".text\n");
        for (MIPSFunction function : functions) {
            sb.append(function.toString());
        }
        return sb.toString();
    }
}
