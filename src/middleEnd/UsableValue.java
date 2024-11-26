package middleEnd;

public interface UsableValue {
    String toValueIR();
    String toLLVMType();
    int toAlign();
    void setRegNo(int regNo);
}
