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

        public int toAlign() {
            switch (s) {
                case "i32":
                    return 4;
                case "i8":
                    return 1;
                default:
                    return 0;
            }
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
