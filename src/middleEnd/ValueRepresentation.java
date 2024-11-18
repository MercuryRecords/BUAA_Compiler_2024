package middleEnd;

public interface ValueRepresentation {
    String toValueIR();
    LLVMType.TypeID toLLVMType();
}
