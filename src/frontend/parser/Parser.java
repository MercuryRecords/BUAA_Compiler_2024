package frontend.parser;

import frontend.ASTNode;
import frontend.LeafASTNode;
import frontend.Token;
import frontend.lexer.LexType;

import java.util.ArrayList;

public class Parser {
    private final ArrayList<Token> tokens;
    private int index = 0;
    // private boolean OUTPUT = false;
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

    private boolean isFuncDef() {
        // TODO
        return false;
    }

    private ASTNode parseFuncDef() {
        // TODO
        return null;
    }

    private ASTNode parseMainFuncDef() {
        // TODO
        return null;
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
        // { ',' <ConstDef> }
        while (curToken().isType(LexType.COMMA)) {
            node.addChild(parseTokenType(LexType.COMMA));
            node.addChild(parseConstDef());
        }
        // ';'
        node.addChild(parseTokenType(LexType.SEMICN));

        return node;
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

    private ASTNode parseIdent() {
        ASTNode node = new ASTNode("Ident");
        if (curToken().isType(LexType.IDENFR)) {
            node.addChild(new LeafASTNode(curToken()));
            nextToken();
            return node;
        }
        return null;
    }

    private ASTNode parseConstExp() {
        // <ConstExp> ::= <AddExp>
        ASTNode node = new ASTNode("ConstExp");
        node.addChild(parseAddExp());
        return node;
    }

    private ASTNode parseExp() {
        // <Exp> ::= <AddExp>
        ASTNode node = new ASTNode("Exp");
        node.addChild(parseAddExp());
        return node;
    }

    private ASTNode parseAddExp() {
        // <AddExp> ::= <MulExp> {<AddOp> <MulExp>}
        ASTNode node = new ASTNode("AddExp");
        node.addChild(parseMulExp());

        while (isAddOp()) {
            node.addChild(parseAddOp());
            node.addChild(parseMulExp());
        }

        return node;
    }

    private boolean isAddOp() {
        return curToken().isType(LexType.PLUS) || curToken().isType(LexType.MINU);
    }

    private ASTNode parseAddOp() {
        if (curToken().isType(LexType.PLUS)) {
            return parseTokenType(LexType.PLUS);
        } else if (curToken().isType(LexType.MINU)) {
            return parseTokenType(LexType.MINU);
        }
        return null;
    }

    private ASTNode parseMulExp() {
        // <MulExp> ::= <UnaryExp> {<MulOp> <UnaryExp>}
        ASTNode node = new ASTNode("MulExp");
        node.addChild(parseUnaryExp());

        while (isMulOp()) {
            node.addChild(parseMulOp());
            node.addChild(parseUnaryExp());
        }

        return node;
    }

    private boolean isMulOp() {
        return curToken().isType(LexType.MULT) || curToken().isType(LexType.DIV) || curToken().isType(LexType.MOD);
    }

    private ASTNode parseMulOp() {
        if (curToken().isType(LexType.MULT)) {
            return parseTokenType(LexType.MULT);
        } else if (curToken().isType(LexType.DIV)) {
            return parseTokenType(LexType.DIV);
        } else if (curToken().isType(LexType.MOD)) {
            return parseTokenType(LexType.MOD);
        }
        return null;
    }

    private ASTNode parseUnaryExp() {
        // <UnaryExp> ::= <PrimaryExp> | <UnaryOp> <UnaryExp> | <Ident> '(' [<FuncFParams>] ')'
        ASTNode node = new ASTNode("UnaryExp");
        if (curToken().isType(LexType.IDENFR) && tokenWithOffset(1).isType(LexType.LPARENT)) {
            node.addChild(parseIdent());
            node.addChild(parseTokenType(LexType.LPARENT));
            if (!curToken().isType(LexType.RPARENT)) {
                node.addChild(parseFuncFParams());
            }
            node.addChild(parseTokenType(LexType.RPARENT));
        } else if (isPrimaryExp()) {
            node.addChild(parsePrimaryExp());
        } else if (isUnaryOp()) {
            node.addChild(parseUnaryOp());
            node.addChild(parseUnaryExp());
        } else {
            node = null;
        }
        return node;
    }

    private ASTNode parseFuncFParams() {
        // FuncFParams ::= <Exp> {',' <Exp>}
        ASTNode node = new ASTNode("FuncFParams");
        node.addChild(parseExp());
        while (curToken().isType(LexType.COMMA)) {
            node.addChild(parseTokenType(LexType.COMMA));
            node.addChild(parseExp());
        }
        return node;
    }

    private boolean isUnaryOp() {
        return curToken().isType(LexType.PLUS) || curToken().isType(LexType.MINU) || curToken().isType(LexType.NOT);
    }

    private ASTNode parseUnaryOp() {
        ASTNode node = new ASTNode("UnaryOp");
        if (curToken().isType(LexType.PLUS)) {
            node.addChild(parseTokenType(LexType.PLUS));
        } else if (curToken().isType(LexType.MINU)) {
            node.addChild(parseTokenType(LexType.MINU));
        } else if (curToken().isType(LexType.NOT)) {
            node.addChild(parseTokenType(LexType.NOT));
        } else {
            node = null;
        }
        return node;
    }

    private boolean isPrimaryExp() {
        return curToken().isType(LexType.LPARENT) || curToken().isType(LexType.IDENFR) || curToken().isType(LexType.INTCON) || curToken().isType(LexType.CHRCON);
    }

    private ASTNode parsePrimaryExp() {
        // <PrimaryExp> ::= '(' <Exp> ')' | <LVal> | <Number> | <Character>
        ASTNode node = new ASTNode("PrimaryExp");
        if (curToken().isType(LexType.LPARENT)) {
            node.addChild(parseTokenType(LexType.LPARENT));
            node.addChild(parseExp());
            node.addChild(parseTokenType(LexType.RPARENT));
        } else if (curToken().isType(LexType.IDENFR)) {
            node.addChild(parseLVal());
        } else if (curToken().isType(LexType.INTCON)) {
            node.addChild(parseNumber());
        } else if (curToken().isType(LexType.CHRCON)) {
            node.addChild(parseChar());
        } else {
            node = null;
        }
        return node;
    }

    private ASTNode parseLVal() {
        // <LVal> ::= <Ident> [ '[' <Exp> ']' ]
        ASTNode node = new ASTNode("LVal");
        node.addChild(parseIdent());
        if (curToken().isType(LexType.LBRACK)) {
            node.addChild(parseTokenType(LexType.LBRACK));
            node.addChild(parseExp());
            node.addChild(parseTokenType(LexType.RBRACK));
        }
        return node;
    }

    private ASTNode parseNumber() {
        // <Number> ::= <IntConst>
        ASTNode node = new ASTNode("Number");
        node.addChild(parseTokenType(LexType.INTCON));
        return node;
    }

    private ASTNode parseChar() {
        // <Character> ::= <CharConst>
        ASTNode node = new ASTNode("Character");
        node.addChild(parseTokenType(LexType.CHRCON));
        return node;
    }

    private ASTNode parseConstInitVal() {
        // <ConstInitVal> ::= <ConstExp> | '{' [ <ConstExp> { ',' <ConstExp> } ] '}' | StringConst
        ASTNode node = new ASTNode("ConstInitVal");
        if (curToken().isType(LexType.LBRACE)) {
            node.addChild(parseTokenType(LexType.LBRACE));
            if (!curToken().isType(LexType.RBRACE)) {
                node.addChild(parseConstExp());
                while (curToken().isType(LexType.COMMA)) {
                    node.addChild(parseTokenType(LexType.COMMA));
                    node.addChild(parseConstExp());
                }
            }
            node.addChild(parseTokenType(LexType.RBRACE));
        } else if (curToken().isType(LexType.STRCON)) {
            node.addChild(parseTokenType(LexType.STRCON));
        } else {
            node.addChild(parseConstExp());
        }
        return node;
    }

    private ASTNode parseVarDecl() {
        // <VarDecl> ::= <BType> <VarDef> { ',' <VarDef> } ';'
        ASTNode node = new ASTNode("VarDecl");
        node.addChild(parseBType());
        node.addChild(parseVarDef());
        while (curToken().isType(LexType.COMMA)) {
            node.addChild(parseTokenType(LexType.COMMA));
            node.addChild(parseVarDef());
        }

        node.addChild(parseTokenType(LexType.SEMICN));
        return node;
    }

    private ASTNode parseVarDef() {
        // <VarDef> ::= <Ident> [ '[' <ConstExp> ']' ] [ '=' <InitVal> ]
        ASTNode node = new ASTNode("VarDef");
        node.addChild(parseIdent());
        if (curToken().isType(LexType.LBRACK)) {
            node.addChild(parseTokenType(LexType.LBRACK));
            node.addChild(parseConstExp());
            node.addChild(parseTokenType(LexType.RBRACK));
        }
        if (curToken().isType(LexType.ASSIGN)) {
            node.addChild(parseTokenType(LexType.ASSIGN));
            node.addChild(parseInitVal());
        }
        return node;
    }

    private ASTNode parseInitVal() {
        // <InitVal> ::= <StringConst> | '{' [ <Exp> { ',' <Exp> } ] '}' | <Exp>
        ASTNode node = new ASTNode("InitVal");
        if (curToken().isType(LexType.STRCON)) {
            node.addChild(parseTokenType(LexType.STRCON));
        } else if (curToken().isType(LexType.LBRACE)) {
            node.addChild(parseTokenType(LexType.LBRACE));
            if (!curToken().isType(LexType.RBRACE)) {
                node.addChild(parseExp());
                while (curToken().isType(LexType.COMMA)) {
                    node.addChild(parseTokenType(LexType.COMMA));
                    node.addChild(parseExp());
                }
            }
            node.addChild(parseTokenType(LexType.RBRACE));
        } else {
            node.addChild(parseExp());
        }
        return node;
    }

    private ASTNode parseTokenType(LexType type) {
        if (!curToken().isType(type)) {
            return null;
        }

        nextToken();
        return new LeafASTNode(tokenWithOffset(-1));
    }
}
