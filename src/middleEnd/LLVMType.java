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
            return switch (s) {
                case "i32" -> 4;
                case "i8" -> 1;
                default -> 0;
            };
        }
    }

    public enum InstType {
        Alloca("alloca"),
        Load("load"),
        Store("store"),

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
