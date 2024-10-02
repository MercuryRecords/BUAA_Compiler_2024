package frontend;

public class CompilerException extends Exception {
    // package-private 写法
    final int lineNum;
    final String message;

    public CompilerException(int lineNum, String message) {
        this.lineNum = lineNum;
        this.message = message;
    }

    @Override
    public String toString() {
        return lineNum + " " + message;
    }
}
