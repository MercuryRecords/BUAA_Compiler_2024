package middleEnd;

import backEnd.Register;

public interface UsableValue {
    String toValueIR();
    String toLLVMType();
    int toAlign();
    void setVirtualRegNo(int regNo);
}
