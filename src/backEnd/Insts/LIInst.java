package backEnd.Insts;

import backEnd.MIPSInst;
import backEnd.Register;

public class LIInst extends MIPSInst {
    private Register target;
    private int val;
    public LIInst(Register target, int val) {
        super();
        this.target = target;
        this.val = val;
    }

    @Override
    public String toString() {
        return "li " + target + ", " + val;
    }
}
