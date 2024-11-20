package middleEnd;

public class LLVMType {
    public enum TypeID {
        VoidTyID("void"),
        IntegerTyID("i32"),
        CharTyID("i8"),
        IntegerPtrTyID("i32*"),
        CharPtrTyID("i8*"),
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
                case "i32*", "i8*" -> 8;
                default -> 0;
            };
        }

        public TypeID toPointerType() {
            return switch (s) {
                case "i32" -> IntegerPtrTyID;
                case "i8" -> CharPtrTyID;
                default -> null;
            };
        }
    }

    public enum InstType {
        LOAD("load"),
        STORE("store"),
        ALLOCA("alloca"),
        GETELEMENTPTR("getelementptr"),

        ADD("add"),
        SUB("sub"),
        MUL("mul"),
        SDIV("sdiv"),
        SREM("srem"),
        AND("and"),
        OR("or"),

        RETURN("ret"),

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
