package middleEnd;

import backEnd.Register;

public interface UsableValue {
    String toValueIR();
    String toLLVMType();
    int toAlign();
    void setRegNo(int regNo);
    boolean useReg();
    int offsetInMemory();
    Register getReg();
}
