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
}
