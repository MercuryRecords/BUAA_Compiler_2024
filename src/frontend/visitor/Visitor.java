package frontend.visitor;

import frontend.*;
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
    private SymbolTable currTable;
    private int symbolId = 1;
    private int scopeId = 1;

    public Visitor(ASTNode node) {
        this.root = node;
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
        if (currTable.hasSymbol("main")) {
            // TODO 错误处理
        }
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
        BType bType = null;
        LeafASTNode leafNode = (LeafASTNode) node.children.get(1).children.get(0);
        if (leafNode.token.isType(LexType.INTTK)) {
            // int
            bType = BType.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            bType = BType.CHAR;
        }

        for (ASTNode child : node.children) {
            if (child.name.equals("ConstDef")) {
                visitConstDef(child, bType);
            }
        }
    }
    private void visitConstDef(ASTNode node, BType bType) {
        // <ConstDef> ::= <Ident> '=' <ConstInitVal> | <Ident> '[' <ConstExp> ']' '=' <ConstInitVal>
        Token token = ((LeafASTNode) node.children.get(0)).token;
        String name = token.token;
        if (currTable.hasSymbol(name)) {
            MyError error = new MyError(token.lineNum, "b");
            Reporter.REPORTER.add(error);
        }
        SymbolType symbolType = null;
        if (node.children.size() == 3) {
            // <Ident> '=' <ConstInitVal>
            symbolType = SymbolType.CONST;
        } else if (node.children.size() == 6) {
            // <Ident> '[' <ConstExp> ']' '=' <ConstInitVal>
            symbolType = SymbolType.CONSTARRAY;
        }
        currTable.addSymbol(new Symbol(symbolId++, currTable.id, token, bType, symbolType));
    }

    private void visitVarDecl(ASTNode node) {
        // <VarDecl> ::= <BType> <VarDef> { ',' <VarDef> } ';'
        // <BType> ::= 'int' | 'char'
        BType bType = null;
        LeafASTNode leafNode = (LeafASTNode) node.children.get(0).children.get(0);
        if (leafNode.token.isType(LexType.INTTK)) {
            // int
            bType = BType.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            bType = BType.CHAR;
        }

        for (ASTNode child : node.children) {
            if (child.name.equals("VarDef"))
                visitVarDef(child, bType);
        }

    }

    private void visitVarDef(ASTNode child, BType bType) {
        // <VarDef> ::= <Ident> [ '[' <ConstExp> ']' ] [ '=' <InitVal> ]
        Token token = ((LeafASTNode) child.children.get(0)).token;
        String name = token.token;
        if (currTable.hasSymbol(name)) {
            MyError error = new MyError(token.lineNum, "b");
            Reporter.REPORTER.add(error);
        }

        SymbolType symbolType = null;
        if (child.children.size() == 1 || child.children.size() == 3) {
            // <Ident> [ '=' <InitVal> ]
            symbolType = SymbolType.VAR;
        } else if (child.children.size() == 4 || child.children.size() == 6) {
            // <Ident> '[' <ConstExp> ']' [ '=' <InitVal> ]
            symbolType = SymbolType.ARRAY;
        }

        currTable.addSymbol(new Symbol(symbolId++, currTable.id, token, bType, symbolType));
    }


    private void visitFuncDef(ASTNode node) {
        // <FuncDef> ::= <FuncType> <Ident> '(' [<FuncFParams>] ')' <Block>
        // <FuncType> ::= 'void' | 'int' | 'char'
        Token token = ((LeafASTNode) node.children.get(1)).token;
        String funcname = token.token;
        if (currTable.hasSymbol(funcname)) {
            MyError error = new MyError(token.lineNum, "b");
            Reporter.REPORTER.add(error);
        }
        SymbolType symbolType = SymbolType.FUNC;
        BType bType = null;
        LeafASTNode leafNode = (LeafASTNode) node.children.get(0).children.get(0);
        if (leafNode.token.isType(LexType.VOIDTK)) {
            // void
            bType = BType.VOID;
        } else if (leafNode.token.isType(LexType.INTTK)) {
            // int
            bType = BType.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            bType = BType.CHAR;
        }
        Symbol symbol = new Symbol(symbolId++, currTable.id, token, bType, symbolType);
        currTable.addSymbol(symbol);
        enterScope();
        if (node.children.size() == 5) {
            visitBlock(node.children.get(4));
        } else {
            ArrayList<Symbol> paras = visitFParams(node.children.get(3));
            symbol.setParas(paras);
            visitBlock(node.children.get(5));
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
        String name = token.token;
        if (currTable.hasSymbol(name)) {
            MyError error = new MyError(token.lineNum, "b");
            Reporter.REPORTER.add(error);
        }

        BType bType = null;
        LeafASTNode leafNode = (LeafASTNode) children.get(0).children.get(0);
        if (leafNode.token.isType(LexType.INTTK)) {
            // int
            bType = BType.INT;
        } else if (leafNode.token.isType(LexType.CHARTK)) {
            // char
            bType = BType.CHAR;
        }

        Symbol symbol;
        if (children.size() == 2) {
            symbol = new Symbol(symbolId++, currTable.id, token, bType, SymbolType.VAR);
            // <BType> <Ident>
        } else {
            // <BType> <Ident> '[' ']'
            symbol = new Symbol(symbolId++, currTable.id, token, bType, SymbolType.ARRAY);
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
                visitCond(children.get(2));
                visitStmt(children.get(4));
                if (children.size() == 7) {
                    visitStmt(children.get(6));
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
        // <LOrExp> ::= <LAndExp> { '||' <LAndExp> }
        for (ASTNode child : node.children) {
            if (child.isNode("LAndExp")) {
                visitLAndExp(child);
            }
        }
    }

    private void visitLAndExp(ASTNode node) {
        // <LAndExp> ::= <EqExp> { '&&' <EqExp> }
        for (ASTNode child : node.children) {
            if (child.isNode("EqExp")) {
                visitEqExp(child);
            }
        }
    }

    private void visitEqExp(ASTNode node) {
        // <EqExp> ::= <RelExp> { ('==' | '!=') <RelExp> }
        for (ASTNode child : node.children) {
            if (child.isNode("RelExp")) {
                visitRelExp(child);
            }
        }
    }

    private void visitRelExp(ASTNode node) {
        // <RelExp> ::= <AddExp> { ('<' | '<=' | '>' | '>=' ) <AddExp> }
        for (ASTNode child : node.children) {
            if (child.isNode("AddExp")) {
                visitAddExp(child);
            }
        }
    }

    private void visitAddExp(ASTNode node) {
        // <AddExp> ::= <MulExp> {<AddOp> <MulExp>}
        for (ASTNode child : node.children) {
            if (child.isNode("MulExp")) {
                visitMulExp(child);
            }
        }
    }

    private void visitMulExp(ASTNode node) {
        // <MulExp> ::= <UnaryExp> {<MulOp> <UnaryExp>}
        for (ASTNode child : node.children) {
            if (child.isNode("UnaryExp")) {
                visitUnaryExp(child);
            }
        }
    }

    private void visitUnaryExp(ASTNode node) {
        // <UnaryExp> ::= <PrimaryExp> | <UnaryOp> <UnaryExp> | <Ident> '(' [<FuncRParams>] ')'
        if (node.children.get(0).isNode("PrimaryExp")) {
            visitPrimaryExp(node.children.get(0));
        } else if (node.children.get(0).isNode("UnaryOp")) {
            visitUnaryExp(node.children.get(1));
        } else if (node.children.get(0).isNode("LEAF")) {
            Token token = ((LeafASTNode) node.children.get(0)).token;
            if (!currTable.hasSymbol(token.token)) {
                MyError error = new MyError(token.lineNum, "c");
                Reporter.REPORTER.add(error);
            }
        }
    }

    private void visitPrimaryExp(ASTNode node) {
        // <PrimaryExp> ::= '(' <Exp> ')' | <LVal> | <Number> | <Character>
        if (node.children.size() == 3) {
            visitExp(node.children.get(1));
        } else if (node.children.get(0).isNode("LVal")) {
            visitLVal(node.children.get(0));
        } else if (node.children.get(0).isNode("Number")) {
            visitNumber(node.children.get(0));
        } else if (node.children.get(0).isNode("Character")) {
            visitCharacter(node.children.get(0));
        }
    }

    private void visitCharacter(ASTNode node) {
        // TODO
    }

    private void visitNumber(ASTNode node) {
        // TODO
    }

    private void visitLVal(ASTNode node) {
        // <LVal> ::= <Ident> [ '[' <Exp> ']' ]
        Token token = ((LeafASTNode) node.children.get(0)).token;
        if (!currTable.hasSymbol(token.token)) {
            MyError error = new MyError(token.lineNum, "c");
            Reporter.REPORTER.add(error);
        }

        if (node.children.size() == 4) {
            visitExp(node.children.get(2));
        }
    }

    private void visitExp(ASTNode node) {
        // <Exp> ::= <AddExp>
        visitAddExp(node.children.get(0));
    }
}
