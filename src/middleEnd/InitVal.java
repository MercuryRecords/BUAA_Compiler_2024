package middleEnd;

import middleEnd.utils.RegTracker;

import java.util.ArrayList;

public class InitVal extends Value {
    private final ArrayList<IRGenerator.LLVMExp> exps = new ArrayList<>();
    // 供 ConstInitVal 使用
    public InitVal() {
        super();
    }

    public void addExp(IRGenerator.LLVMExp llvmExp) {
        exps.add(llvmExp);
    }

    public IRGenerator.LLVMExp get(int i) {
        return exps.get(i);
    }

    public int size() {
        return exps.size();
    }
}
