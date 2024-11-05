import frontend.Token;
import frontend.ASTNode;
import frontend.Reporter;
import frontend.lexer.Lexer;
import frontend.parser.Parser;
import frontend.visitor.Visitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Compiler {
    private static final String forInput = "testfile.txt";
    private static final String forLexer = "lexer.txt";
    private static final String forParser = "parser.txt";
    private static final String forVisitor = "visitor.txt";

    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(forInput)));
            Lexer lexer = new Lexer(content);
            ArrayList<Token> tokens = lexer.analyze(forLexer);
            Parser parser = new Parser(tokens);
            ASTNode node = parser.analyze(forParser);
            Visitor visitor = new Visitor(node);
            visitor.analyze(forVisitor);
            Reporter.REPORTER.report();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
