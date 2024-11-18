package middleEnd;

public interface ValueRepresentation {
    String toValueIR();
    String toLLVMType();
    int toAlign();
}
