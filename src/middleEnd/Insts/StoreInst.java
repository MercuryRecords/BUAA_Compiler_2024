package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.UsableValue;

public class StoreInst extends Instruction {

    private final UsableValue from;
    private final UsableValue to;

    public StoreInst(UsableValue from, UsableValue to) {
        super(LLVMType.InstType.STORE);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return String.format("store %s %s, %s* %s, align %d", from.toLLVMType(), from.toValueIR(), to.toLLVMType(), to.toValueIR(), to.toAlign());
    }
}
