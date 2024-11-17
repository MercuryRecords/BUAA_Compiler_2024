package middleEnd;

public class Instruction extends Value {
    public LLVMType.InstType type;
    public Instruction(LLVMType.InstType type) {
        this.type = type;
    }
}
