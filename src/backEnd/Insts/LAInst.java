package backEnd.Insts;

import backEnd.MIPSInst;
import backEnd.Register;

public class LAInst extends MIPSInst {
    private final Register register;
    private final String valueIR;
    public LAInst(Register register, String valueIR) {
        super();
        this.register = register;
        this.valueIR = valueIR;
    }

    @Override
    public String toString() {
        return "la " + register.toString() + ", " + valueIR;
    }
}
