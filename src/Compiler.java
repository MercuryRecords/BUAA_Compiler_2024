import frontend.lexer.Lexer;
import frontend.parser.Parser;
import frontend.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Compiler {
    private static final String forInput = "testfile.txt";
    private static final String forOutput = "parser.txt";
    private static final String forError = "error.txt";

    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(forInput)));
            Lexer lexer = new Lexer(content);
            ArrayList<Token> tokens = lexer.analyze(forError);
            Parser parser = new Parser(tokens);
            parser.analyze(forOutput, forError);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
