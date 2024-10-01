package frontend;

public class CompilerException extends Exception {
    private final int lineNum;
    private final String message;

    public CompilerException(int lineNum, String message) {
        this.lineNum = lineNum;
        this.message = message;
    }

    @Override
    public String toString() {
        return lineNum + " " + message;
    }
}
