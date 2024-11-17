package middleEnd;

public class Instruction extends User {
    public LLVMType.InstType type;
    public Instruction(LLVMType.InstType type) {
        this.type = type;
    }
}
