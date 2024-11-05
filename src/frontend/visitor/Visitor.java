package frontend.visitor;

import frontend.*;
import frontend.Symbol._SymbolType1;
import frontend.Symbol._SymbolType2;
import frontend.Symbol.SymbolType;
import frontend.lexer.LexType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Visitor {
    private final ASTNode root;
    private final Stack<SymbolTable> symbolTableStack = new Stack<>();
    private final ArrayList<SymbolTable> allSymbolTables = new ArrayList<>();
    private SymbolTable currTable = null;
    private int symbolId = 1;
    private int scopeId = 1;

    public Visitor(ASTNode node) {
        this.root = node;
    }

    private SymbolType getSymbolType(Token token) {
        SymbolTable table = currTable;
        while (table != null) {
            if (table.hasSymbol(token.token)) {
                return table.getSymbol(token.token).symbolType;
            }

            table = table.parentTable;
        }

        return SymbolType.ERROR;
    }

    private void checkErrorB(Token token) {
        if (currTable.hasSymbol(token.token)) {
            Reporter.REPORTER.add(new MyError(token.lineNum, "b"));
        }
    }

    private void checkErrorC(Token token) {
        SymbolTable table = currTable;
        while (table != null) {
            if (table.hasSymbol(token.token)) {
                return;
            }

            table = table.parentTable;
        }

        Reporter.REPORTER.add(new MyError(token.lineNum, "c"));
    }


    public void analyze(String forOutput) {
        visitCompUnit(root);
        ArrayList<Symbol> symbols = new ArrayList<>();
        for (SymbolTable table : allSymbolTables) {
            symbols.addAll(table.getAllSymbols());
        }

        // symbols 排序后输出
        symbols.sort((a, b) -> {
            if (a.tableId != b.tableId) {
                return a.tableId - b.tableId;
            } else
                return a.id - b.id;
        });

        try (FileWriter writer = new FileWriter(forOutput)) {
            for (Symbol symbol : symbols) {
                writer.write(symbol + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void enterScope() {
        SymbolTable newTable = new SymbolTable(scopeId++, currTable);
        symbolTableStack.push(newTable);
        allSymbolTables.add(newTable);
        currTable = newTable;
    }

    private void exitScope() {
        symbolTableStack.pop();
        try {
            currTable = symbolTableStack.peek();
        } catch (EmptyStackException e) {
            currTable = null;
        }
    }

    private void visitCompUnit(ASTNode node) {
        // <CompUnit> ::= {<Decl>} {<FuncDef>} <MainFuncDef>
        enterScope();
        ArrayList<ASTNode> children = node.children;
        int i = 0;
        while (i < children.size() && children.get(i).isNode("Decl")) {
            visitDecl(children.get(i));

            // visitConstDecl(children.get(i));
            i++;
        }
        while (i < children.size() && children.get(i).isNode("FuncDef")) {
            visitFuncDef(children.get(i));
            i++;
        }
        if (i < children.size() && children.get(i).isNode("MainFuncDef")) {
            visitMainFuncDef(children.get(i));
        } else {
            // TODO 错误处理
        }
        exitScope();
    }
    private void visitMainFuncDef(ASTNode node) {
        // <MainFuncDef> ::= 'int' 'main' '(' ')' <Block>
        //if (currTable.hasSymbol("main")) {
        //    // TODO 错误处理
        //}
        // Token token = ((LeafASTNode) node.children.get(1)).token;
        // currTable.addSymbol(new Symbol(symbolId++, currTable.id, token, BType.INT, SymbolType.FUNC));
        enterScope();
        visitBlock(node.children.get(4));
        exitScope();
    }

    private void visitDecl(ASTNode node) {
        // <Decl> ::= <ConstDecl> | <VarDecl>
        node = node.children.get(0);
        if (node.isNode("ConstDecl")) {
            visitConstDecl(node);
        } else if (node.isNode("VarDecl")) {
            visitVarDecl(node);
        }
    }


    private void visitConstDecl(ASTNode node) {
        // <ConstDecl> ::= 'const' <BType> <ConstDef> { ',' <ConstDef> } ';'
        // <BType> ::= 'int' | 'char'
        _SymbolType1 symbolType1 = null;
        LeafASTNode leafNode = (LeafASTNode) node.children.get(1).children.get(0);
        if (leafNode.token.isType(LexType.INTTK)) {
            // int
            symbolType1 = _SymbolType1.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            symbolType1 = _SymbolType1.CHAR;
        }

        for (ASTNode child : node.children) {
            if (child.name.equals("ConstDef")) {
                visitConstDef(child, symbolType1);
            }
        }
    }
    private void visitConstDef(ASTNode node, _SymbolType1 symbolType1) {
        // <ConstDef> ::= <Ident> '=' <ConstInitVal> | <Ident> '[' <ConstExp> ']' '=' <ConstInitVal>
        Token token = ((LeafASTNode) node.children.get(0)).token;
        String name = token.token;
        checkErrorB(token);
        _SymbolType2 symbolType2 = null;
        if (node.children.size() == 3) {
            // <Ident> '=' <ConstInitVal>
            symbolType2 = _SymbolType2.CONST;
        } else if (node.children.size() == 6) {
            // <Ident> '[' <ConstExp> ']' '=' <ConstInitVal>
            symbolType2 = _SymbolType2.CONSTARRAY;
        }
        currTable.addSymbol(new Symbol(symbolId++, currTable.id, token, symbolType1, symbolType2));
    }

    private void visitVarDecl(ASTNode node) {
        // <VarDecl> ::= <BType> <VarDef> { ',' <VarDef> } ';'
        // <BType> ::= 'int' | 'char'
        _SymbolType1 symbolType1 = null;
        LeafASTNode leafNode = (LeafASTNode) node.children.get(0).children.get(0);
        if (leafNode.token.isType(LexType.INTTK)) {
            // int
            symbolType1 = _SymbolType1.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            symbolType1 = _SymbolType1.CHAR;
        }

        for (ASTNode child : node.children) {
            if (child.name.equals("VarDef"))
                visitVarDef(child, symbolType1);
        }

    }

    private void visitVarDef(ASTNode child, _SymbolType1 symbolType1) {
        // <VarDef> ::= <Ident> [ '[' <ConstExp> ']' ] [ '=' <InitVal> ]
        Token token = ((LeafASTNode) child.children.get(0)).token;
        String name = token.token;
        checkErrorB(token);

        _SymbolType2 symbolType2 = null;
        if (child.children.size() == 1 || child.children.size() == 3) {
            // <Ident> [ '=' <InitVal> ]
            symbolType2 = _SymbolType2.VAR;
        } else {
            // <Ident> '[' <ConstExp> ']' [ '=' <InitVal> ]
            symbolType2 = _SymbolType2.ARRAY;
        }

        currTable.addSymbol(new Symbol(symbolId++, currTable.id, token, symbolType1, symbolType2));
    }


    private void visitFuncDef(ASTNode node) {
        // <FuncDef> ::= <FuncType> <Ident> '(' [<FuncFParams>] ')' <Block>
        // <FuncType> ::= 'void' | 'int' | 'char'
        Token token = ((LeafASTNode) node.children.get(1)).token;

        checkErrorB(token);

        _SymbolType2 symbolType2 = _SymbolType2.FUNC;
        _SymbolType1 symbolType1 = null;
        LeafASTNode leafNode = (LeafASTNode) node.children.get(0).children.get(0);
        if (leafNode.token.isType(LexType.VOIDTK)) {
            // void
            symbolType1 = _SymbolType1.VOID;
        } else if (leafNode.token.isType(LexType.INTTK)) {
            // int
            symbolType1 = _SymbolType1.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            symbolType1 = _SymbolType1.CHAR;
        }
        Symbol symbol = new Symbol(symbolId++, currTable.id, token, symbolType1, symbolType2);
        currTable.addSymbol(symbol);
        enterScope();
        if (node.children.get(4).isNode("FuncFParams")) {
            ArrayList<Symbol> paras = visitFParams(node.children.get(3));
            symbol.setParas(paras);
        }
        for (ASTNode child : node.children) {
            if (child.isNode("Block")) {
                visitBlock(child);
            }
        }
        exitScope();
    }

    private ArrayList<Symbol> visitFParams(ASTNode node) {
        // FuncFParams ::= <FuncFParam> {',' <FuncFParam>}
        ArrayList<Symbol> paras = new ArrayList<>();
        for (ASTNode child : node.children) {
            if (child.isNode("FuncFParam")) {
                paras.add(visitFuncFParam(child));
            }
        }
        return paras;
    }

    private Symbol visitFuncFParam(ASTNode node) {
        // FuncFParam ::= <BType> <Ident> [ '[' ']' ]
        ArrayList<ASTNode> children = node.children;

        Token token = ((LeafASTNode) children.get(1)).token;
        checkErrorB(token);

        _SymbolType1 symbolType1 = null;
        LeafASTNode leafNode = (LeafASTNode) children.get(0).children.get(0);
        if (leafNode.token.isType(LexType.INTTK)) {
            // int
            symbolType1 = _SymbolType1.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            symbolType1 = _SymbolType1.CHAR;
        }

        Symbol symbol;
        if (children.size() == 2) {
            symbol = new Symbol(symbolId++, currTable.id, token, symbolType1, _SymbolType2.VAR);
            // <BType> <Ident>
        } else {
            // <BType> <Ident> '[' ']'
            symbol = new Symbol(symbolId++, currTable.id, token, symbolType1, _SymbolType2.ARRAY);
        }
        currTable.addSymbol(symbol);

        return symbol;

    }

    private void visitBlock(ASTNode node) {
        // <Block> ::= '{' { <BlockItem> } '}'
        for (ASTNode child : node.children) {
            if (child.isNode("BlockItem")) {
                visitBlockItem(child.children.get(0));
            }
        }
    }

    private void visitBlockItem(ASTNode node) {
        // <BlockItem> ::= <Stmt> | <Decl>
        if (node.isNode("Stmt")) {
            visitStmt(node);
        } else if (node.isNode("Decl"))
            visitDecl(node);
    }

    private void visitStmt(ASTNode node) {
        /*
           <Stmt> ::= <Block>
                    | 'if' '(' <Cond> ')' <Stmt> [ 'else' <Stmt> ]
                    | 'for' '(' [ <ForStmt> ] ';' [ <Cond> ] ';' [ <ForStmt> ] ')' <Stmt>
                    | 'break' ';'
                    | 'continue' ';'
                    | 'return' [ <Exp> ] ';'
                    | 'printf' '(' <StringConst> { ',' Exp } ')' ';'
                    | <LVal> '=' 'getint' '(' ')' ';'
                    | <LVal> '=' 'getchar' '(' ')' ';'
                    | <LVal> '=' <Exp> ';'
                    | [Exp] ';'
         */
        ArrayList<ASTNode> children = node.children;
        if (children.get(0).isNode("Block")) {
            enterScope();
            visitBlock(children.get(0));
            exitScope();
        } else if (children.get(0).isNode("LEAF")) {
            Token token = ((LeafASTNode) children.get(0)).token;
            if (token.isType(LexType.IFTK)) {
                int i = 2;
                visitCond(children.get(i++));
                while (i < children.size()) {
                    if (children.get(i).isNode("Stmt")) {
                        visitStmt(children.get(i));
                    }
                    i++;
                }
            } else if (token.isType(LexType.FORTK)) {
                int i = 2;
                if (i < children.size() && children.get(i).isNode("ForStmt")) {
                    visitForStmt(children.get(i));
                    i++;
                }
                i++;
                if (i < children.size() && children.get(i).isNode("Cond")) {
                    visitCond(children.get(i));
                    i++;
                }
                i++;
                if (i < children.size() && children.get(i).isNode("ForStmt")) {
                    visitForStmt(children.get(i));
                    i++;
                }
                i++;
                visitStmt(children.get(i));
            } else if (token.isType(LexType.BREAKTK)) {
                // TODO
            } else if (token.isType(LexType.CONTINUETK)) {
                // TODO
            } else if (token.isType(LexType.RETURNTK)) {
                // TODO
            } else if (token.isType(LexType.PRINTFTK)) {
                // TODO
            }
        } else if (children.get(0).isNode("LVal")) {
            visitLVal(children.get(0));
            if (children.get(2).isNode("getint")) {
                // TODO
            } else if (children.get(2).isNode("getchar")) {
                // TODO
            } else if (children.get(2).isNode("Exp")) {
                // TODO
            }
        } else if (children.get(0).isNode("Exp")) {
            // TODO
        }
    }

    private void visitForStmt(ASTNode node) {
        // <ForStmt> ::=  <LVal> '=' <Exp>
        visitLVal(node.children.get(0));
        visitExp(node.children.get(2));
    }

    private void visitCond(ASTNode node) {
        // <Cond> ::= <LOrExp>
        visitLOrExp(node.children.get(0));
    }

    private void visitLOrExp(ASTNode node) {
        // <LOrExp> ::= <LOrExp> '||' <LAndExp>  | <LAndExp>
        if (node.children.size() == 1) {
            visitLAndExp(node.children.get(0));
        } else {
            visitLOrExp(node.children.get(0));
            visitLAndExp(node.children.get(2));
        }
    }

    private void visitLAndExp(ASTNode node) {
        // <LAndExp> ::= <LAndExp> '&&' <EqExp>  | <EqExp>
        if (node.children.size() == 1) {
            visitEqExp(node.children.get(0));
        } else {
            visitLAndExp(node.children.get(0));
            visitEqExp(node.children.get(2));
        }
    }

    private void visitEqExp(ASTNode node) {
        // <EqExp> ::= <EqExp> ('==' | '!=') <RelExp> | <RelExp>
        if (node.children.size() == 1) {
            visitRelExp(node.children.get(0));
        } else {
            visitEqExp(node.children.get(0));
            visitRelExp(node.children.get(2));
        }
    }

    private void visitRelExp(ASTNode node) {
        // <RelExp> ::= <RelExp> ('<' | '<=' | '>' | '>=' ) <AddExp> | <AddExp>
        if (node.children.size() == 1) {
            visitAddExp(node.children.get(0));
        } else {
            visitRelExp(node.children.get(0));
            visitAddExp(node.children.get(2));
        }
    }

    private SymbolType visitAddExp(ASTNode node) {
        // <AddExp> ::= <AddExp> <AddOp> <MulExp> | <MulExp>
        if (node.children.size() == 1) {
            return visitMulExp(node.children.get(0));
        } else {
            visitAddExp(node.children.get(0));
            visitMulExp(node.children.get(2));
            return SymbolType.Int;
        }
    }

    private SymbolType visitMulExp(ASTNode node) {
        // <MulExp> ::= <MulExp> <MulOp> <UnaryExp> | <UnaryExp>
        if (node.children.size() == 1) {
            return visitUnaryExp(node.children.get(0));
        } else {
            visitMulExp(node.children.get(0));
            visitUnaryExp(node.children.get(2));
            return SymbolType.Int;
        }
    }

    private SymbolType visitUnaryExp(ASTNode node) {
        // <UnaryExp> ::= <PrimaryExp> | <UnaryOp> <UnaryExp> | <Ident> '(' [<FuncRParams>] ')'
        if (node.children.get(0).isNode("PrimaryExp")) {
            return visitPrimaryExp(node.children.get(0));
        } else if (node.children.get(0).isNode("UnaryOp")) {
            return visitUnaryExp(node.children.get(1));
        } else if (node.children.get(0).isNode("LEAF")) {
            Token token = ((LeafASTNode) node.children.get(0)).token;

            checkErrorC(token);

            ArrayList<SymbolType> paras;
            if (2 < node.children.size() && node.children.get(2).isNode("FuncRParams")) {
                paras = visitFuncRParams(node.children.get(2));
            } else {
                paras = new ArrayList<>();
            }

            // TODO

            return getSymbolType(token);
        }
        return SymbolType.ERROR;
    }

    private ArrayList<SymbolType> visitFuncRParams(ASTNode node) {
        // FuncRParams ::= <Exp> {',' <Exp>}
        ArrayList<SymbolType> paras = new ArrayList<>();
        for (ASTNode child : node.children) {
            if (child.isNode("Exp")) {
                paras.add(visitExp(child));
            }
        }
        return paras;
    }

    private SymbolType visitPrimaryExp(ASTNode node) {
        // <PrimaryExp> ::= '(' <Exp> ')' | <LVal> | <Number> | <Character>
        if (node.children.size() > 1) {
            return visitExp(node.children.get(1));
        } else if (node.children.get(0).isNode("LVal")) {
            return visitLVal(node.children.get(0));
        } else if (node.children.get(0).isNode("Number")) {
            return SymbolType.ConstInt;
        } else if (node.children.get(0).isNode("Character")) {
            return SymbolType.ConstChar;
        }
        return SymbolType.ERROR;
    }

    private SymbolType visitLVal(ASTNode node) {
        // <LVal> ::= <Ident> [ '[' <Exp> ']' ]
        Token token = ((LeafASTNode) node.children.get(0)).token;
        checkErrorC(token);

        return getSymbolType(token);
    }

    private SymbolType visitExp(ASTNode node) {
        // <Exp> ::= <AddExp>
        return visitAddExp(node.children.get(0));
    }
}
