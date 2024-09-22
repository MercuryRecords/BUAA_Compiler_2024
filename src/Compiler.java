import frontend.Lexer.Lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compiler {
    private static final String forInput = "testfile.txt";
    private static final String forOutput = "lexer.txt";
    private static final String forError = "error.txt";

    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(forInput)));
            Lexer lexer = new Lexer(content);
            lexer.analyze(forOutput, forError);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
