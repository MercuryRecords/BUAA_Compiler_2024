package frontend.visitor;

import frontend.*;
import frontend.lexer.LexType;

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

        for (Symbol symbol : symbols) {
            System.out.println(symbol);
        }
    }

    private void enterScope() {
        SymbolTable newTable = new SymbolTable(scopeId++, currTable);
        if (scopeId == 3) {
            System.out.println("enter scope 3");
        }
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
            // TODO 重复定义，错误处理
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
            // TODO 重复定义，错误处理
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
            // TODO 重复定义，错误处理
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
            // TODO 重复定义，错误处理
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

    }


}
