package middleEnd;

public class LLVMType {
    public enum TypeID {
        VoidTyID("void"),
        IntegerTyID("i32"),
        CharTyID("i8"),
        ;

        private final String s;
        TypeID(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }

    public enum InstType {
        AllocaInst("alloca"),
        LoadInst("load"),
        StoreInst("store"),

        ;
        private final String s;
        InstType(String s) {
            this.s = s;
        }
        @Override
        public String toString() {
            return s;
        }
    }
}
