package frontend.parser;

import frontend.ASTNode;
import frontend.LeafASTNode;
import frontend.Token;
import frontend.lexer.LexType;

import java.util.ArrayList;

public class Parser {
    private final ArrayList<Token> tokens;
    private int index = 0;
    private boolean OUTPUT = false;
    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public void analyze(String forOutput) {
        ASTNode root = parseCompUnit();
    }

    private Token curToken() {
        return tokens.get(index);
    }

    private Token tokenWithOffset(int offset) {
        return tokens.get(index + offset);
    }

    private void nextToken() {
        index++;
    }

    private ASTNode parseCompUnit() {
        // <CompUnit> ::= {<Decl>} {<FuncDef>} <MainFuncDef>
        ASTNode node = new ASTNode("CompUnit");

        // {<Decl>}
        while (isDecl()) {
            node.addChild(parseDecl());
        }
        
        // {<FuncDef>}
        while (isFuncDef()) {
            node.addChild(parseFuncDef());
        }

        // <MainFuncDef>
        node.addChild(parseMainFuncDef());
        return node;
    }

    private ASTNode parseMainFuncDef() {
        // TODO
        return null;
    }

    private ASTNode parseFuncDef() {
        // TODO
        return null;
    }

    private boolean isFuncDef() {
        // TODO
        return false;
    }

    private boolean isDecl() {
        // TODO
        return false;
    }

    private ASTNode parseDecl() {
        // <Decl> ::= <ConstDecl> | <VarDecl>
        ASTNode node = new ASTNode("Decl");
        if (curToken().isType(LexType.CONSTTK)) {
            node.addChild(parseConstDecl());
        } else {
            node.addChild(parseVarDecl());
        }

        return node;
    }

    private ASTNode parseConstDecl() {
        // <ConstDecl> ::= 'const' <BType> <ConstDef> { ',' <ConstDef> } ';'
        ASTNode node = new ASTNode("ConstDecl");
        // 'const'
        node.addChild(parseTokenType(LexType.CONSTTK));
        // <BType>
        node.addChild(parseBType());
        // <ConstDef>
        node.addChild(parseConstDef());

        return node;
    }

    private ASTNode parseConstDef() {
        // <ConstDef> ::= <Ident> '=' <ConstInitVal> | <Ident> '[' <ConstExp> ']' '=' <ConstInitVal>
        ASTNode node = new ASTNode("ConstDef");
        // <Ident>
        node.addChild(parseIdent());
        // [ '[' <ConstExp> ']' ]
        if (curToken().isType(LexType.LBRACK)) {
            node.addChild(parseTokenType(LexType.LBRACK));
            node.addChild(parseConstExp());
            node.addChild(parseTokenType(LexType.RBRACK));
        }
        // '='
        node.addChild(parseTokenType(LexType.EQL));
        // <ConstInitVal>
        node.addChild(parseConstInitVal());

        return node;
    }

    private ASTNode parseConstExp() {
        // TODO
        return null;
    }

    private ASTNode parseConstInitVal() {
        // TODO
        return null;
    }

    private ASTNode parseIdent() {
        ASTNode node = new ASTNode("Ident");
        if (curToken().isType(LexType.IDENFR)) {
            node.addChild(new LeafASTNode(curToken()));
            nextToken();
            return node;
        }
        return null;
    }

    private ASTNode parseBType() {
        // <BType> ::= 'int' | 'char'
        ASTNode node = new ASTNode("BType");
        if (curToken().isType(LexType.INTTK) || curToken().isType(LexType.CHARTK)) {
            node.addChild(new LeafASTNode(curToken()));
            nextToken();
            return node;
        }
        return null;
    }

    private ASTNode parseVarDecl() {
        // <VarDecl> ::= <BType> <VarDef> { ',' <VarDef> } ';'
        return null;
    }

    private ASTNode parseTokenType(LexType type) {
        if (!curToken().isType(type)) {
            return null;
        }

        nextToken();
        return new LeafASTNode(tokenWithOffset(-1));
    }
}
