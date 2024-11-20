package middleEnd;

public class LLVMType {
    public enum TypeID {
        VoidTyID("void"),
        LongTyID("i64"),
        IntegerTyID("i32"),
        IntegerPtrTyID("i32*"),
        IntegerPtrPtrTyID("i32**"),
        CharTyID("i8"),
        CharPtrTyID("i8*"),
        CharPtrPtrTyID("i8**"),
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
                case "i32*", "i8*", "i64" -> 8;
                default -> 0;
            };
        }

        public TypeID toPointerType() {
            return switch (this) {
                case IntegerPtrTyID -> IntegerPtrPtrTyID;
                case IntegerTyID -> IntegerPtrTyID;
                case CharPtrTyID -> CharPtrPtrTyID;
                case CharTyID -> CharPtrTyID;
                default -> this;
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

        ZEXT("zext"),
        TRUNC("trunc"),
        CALL("call"),

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
