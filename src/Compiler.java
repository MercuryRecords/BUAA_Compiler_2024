import frontend.lexer.Lexer;
import frontend.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Compiler {
    private static final String forInput = "testfile.txt";
    private static final String forLexer = "lexer.txt";
    public static final String forError = "error.txt";

    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(forInput)));
            Lexer lexer = new Lexer(content);
            ArrayList<Token> tokens = lexer.analyze(forLexer, forError);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
