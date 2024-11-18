package middleEnd.Insts;

import middleEnd.Instruction;
import middleEnd.LLVMType;
import middleEnd.ValueRepresentation;

public class StoreInst extends Instruction {

    private final ValueRepresentation from;
    private final ValueRepresentation to;

    public StoreInst(ValueRepresentation from, ValueRepresentation to) {
        super(LLVMType.InstType.STORE);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return String.format("store %s %s, %s* %s, align %d", from.toLLVMType(), from.toValueIR(), to.toLLVMType(), to.toValueIR(), to.toAlign());
    }
}
