import frontEnd.SymbolTable;
import frontEnd.Token;
import frontEnd.ASTNode;
import frontEnd.Reporter;
import frontEnd.lexer.Lexer;
import frontEnd.parser.Parser;
import frontEnd.visitor.Visitor;
import middleEnd.IRGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Compiler {
    private static final String forInput = "main.c"; // TODO 提交前换回 testfile.txt
    private static final String forLexer = "lexer.txt";
    private static final String forParser = "parser.txt";
    private static final String forVisitor = "symbol.txt";
    private static final String forIR = "llvm_ir.ll";

    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(forInput)));
            Lexer lexer = new Lexer(content);
            ArrayList<Token> tokens = lexer.analyze(forLexer);
            Parser parser = new Parser(tokens);
            ASTNode node = parser.analyze(forParser);
            Visitor visitor = new Visitor(node);
            HashMap<Integer, SymbolTable> symbolTables = visitor.analyze(forVisitor);
            Reporter.REPORTER.report();
            IRGenerator irGenerator = new IRGenerator(node, symbolTables);
            irGenerator.translate(forIR);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
