package middleEnd;

import frontEnd.ASTNode;
import frontEnd.LeafASTNode;
import frontEnd.Symbol;
import frontEnd.SymbolTable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class IRGenerator {
    ASTNode root;
    HashMap<Integer, SymbolTable> symbolTables;
    private int scopeId = 1;
    public IRGenerator(ASTNode root, HashMap<Integer, SymbolTable> symbolTables) {
        this.root = root;
        this.symbolTables = symbolTables;
    }

    public void translate(String forOutput) {
        Module module = translateModule(root);

        try (FileWriter writer = new FileWriter(forOutput)) {

            /*
            declare i32 @getint()          ; 读取一个整数
            declare i32 @getchar()         ; 读取一个字符
            declare void @putint(i32)      ; 输出一个整数
            declare void @putch(i32)       ; 输出一个字符
            declare void @putstr(i8*)      ; 输出字符串
             */
             writer.write("""
                     declare i32 @getint()          ; 读取一个整数
                     declare i32 @getchar()         ; 读取一个字符
                     declare void @putint(i32)      ; 输出一个整数
                     declare void @putch(i32)       ; 输出一个字符
                     declare void @putstr(i8*)      ; 输出字符串""");
            writer.write(module.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private Symbol getSymbol(LeafASTNode node) {
        String identName = node.token.token;
        return symbolTables.get(scopeId).getSymbol(identName);
    }

    private Module translateModule(ASTNode root) {
        Module module = new Module();
        ArrayList<ASTNode> children = root.children;
        for (ASTNode child : children) {
            if (child.isNode("Decl")) {
                module.addGlobalValues(translateDecl(child));
            } else if (child.isNode("FuncDef")) {
                module.addGlobalValue(translateFuncDef(child));
            } else if (child.isNode("MainFuncDef")) {
                module.addGlobalValue(translateMainFuncDef(child));
            }
        }

        return module;
    }

    private LinkedList<GlobalValue> translateDecl(ASTNode node) {
        ASTNode child = node.children.get(0);
        if (child.isNode("ConstDecl")) {
            return translateConstDecl(child);
        } else {
            return translateVarDecl(child);
        }
    }

    private LinkedList<GlobalValue> translateConstDecl(ASTNode node) {
        LinkedList<GlobalValue> values = new LinkedList<>();

        for (ASTNode child : node.children) {
            if (child.isNode("ConstDef")) {
                LeafASTNode ident = (LeafASTNode) child.children.get(0);
                Symbol symbol = getSymbol(ident);
                // System.out.println(symbol.symbolType);
                ASTNode initVal = child.children.get(2);
                Value value = translateConstInitVal(initVal);
                values.add(new GlobalVariable(symbol, value));
            }
        }

        return values;
    }

    private Value translateConstInitVal(ASTNode initVal) {
        // TODO
        return new Value();
    }

    private LinkedList<GlobalValue> translateVarDecl(ASTNode node) {
        LinkedList<GlobalValue> values = new LinkedList<>();
        // TODO
        return values;
    }

    private Function translateMainFuncDef(ASTNode node) {
        Function main = new Function();

        // TODO

        return main;
    }

    private Function translateFuncDef(ASTNode node) {
        Function function = new Function();

        // TODO

        return function;
    }
}
