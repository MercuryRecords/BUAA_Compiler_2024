package middleEnd;

import frontEnd.ASTNode;
import frontEnd.LeafASTNode;
import frontEnd.Symbol;
import frontEnd.SymbolTable;
import middleEnd.utils.GlobalCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IRGenerator {
    ASTNode root;
    HashMap<Integer, SymbolTable> oldSymbolTables;
    private int scopeId = 1;
    private final HashSet<Integer> usedScopeId = new HashSet<>();
    private final GlobalCalculator constCalculator;
    public IRGenerator(ASTNode root, HashMap<Integer, SymbolTable> oldSymbolTables) {
        this.root = root;
        this.oldSymbolTables = oldSymbolTables;
        this.constCalculator = new GlobalCalculator();
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
                     declare void @putstr(i8*)      ; 输出字符串
                     
                     """);
            writer.write(module.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void enterScope() {
        do {
            scopeId++;
        } while (usedScopeId.contains(scopeId));
    }

    private void exitScope() {
        usedScopeId.add(scopeId);
        scopeId = oldSymbolTables.get(scopeId).parentTable.id;
    }

    private Symbol getSymbol(LeafASTNode node) {
        String identName = node.token.token;
        return oldSymbolTables.get(scopeId).getSymbol(identName);
    }

    private Module translateModule(ASTNode root) {
        Module module = new Module();
        ArrayList<ASTNode> children = root.children;
        for (ASTNode child : children) {
            if (child.isNode("Decl")) {
                module.addGlobalValues(translateGlobalDecl(child));
            } else if (child.isNode("FuncDef")) {
                module.addGlobalValue(translateFuncDef(child));
            } else if (child.isNode("MainFuncDef")) {
                module.addGlobalValue(translateMainFuncDef(child));
            }
        }

        return module;
    }

    private LinkedList<GlobalValue> translateGlobalDecl(ASTNode node) {
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
                if (symbol.symbolType.toString().endsWith("Array")) {
                    int arrayLength = calculateConstExp(child.children.get(2));
                    ConstInitVal constInitVal = translateConstInitVal(child.children.get(5));
                    GlobalVariable var = new GlobalVariable(symbol, arrayLength, constInitVal);
                    constCalculator.add(var);
                    values.add(var);
                } else {
                    ConstInitVal constInitVal = translateConstInitVal(child.children.get(2));
                    GlobalVariable var = new GlobalVariable(symbol, constInitVal);
                    constCalculator.add(var);
                    values.add(var);
                }
            }
        }
        return values;
    }

    private int calculateConstExp(ASTNode node) {
        // ConstExp      ::= AddExp，但文法规定使用的 Ident 必须是常量，故可以计算得出
        return constCalculator.calculateConstExp(node);
    }

    private ConstInitVal translateConstInitVal(ASTNode node) {
        // 形式上包括常量初值 ConstInitVal 和变量初值 InitVal，但全局变量的初值表达式必须是常量表达式 ConstExp
        // ConstInitVal  ::= ConstExp | '{' ConstExp { ',' ConstExp } '}' | StringConst
        // InitVal       ::= Exp | '{' Exp { ',' Exp } '}' | StringConst
        // ConstExp      ::= AddExp
        // Exp           ::= AddExp
        // TODO
        if (node.children.get(0).isNode("StringConst")) {
            return new ConstInitVal(((LeafASTNode) node.children.get(0)).token.token);
        } else {
            ConstInitVal constInitVal = new ConstInitVal();
            for (ASTNode child : node.children) {
                if (child.isNode("ConstExp") || child.isNode("Exp")) {
                    constInitVal.addConstExp(calculateConstExp(child));
                }
            }
            return constInitVal;
        }
    }

    private LinkedList<GlobalValue> translateVarDecl(ASTNode node) {
        LinkedList<GlobalValue> values = new LinkedList<>();

        for (ASTNode child : node.children) {
            if (child.isNode("VarDef")) {
                LeafASTNode ident = (LeafASTNode) child.children.get(0);
                Symbol symbol = getSymbol(ident);
                if (symbol.symbolType.toString().endsWith("Array")) {
                    int arrayLength = calculateConstExp(child.children.get(2));
                    if (child.children.get(child.children.size() - 1).isNode("InitVal")) {
                        ConstInitVal constInitVal = translateConstInitVal(child.children.get(5));
                        values.add(new GlobalVariable(symbol, arrayLength, constInitVal));
                    } else {
                        values.add(new GlobalVariable(symbol, arrayLength));
                    }
                } else {
                    if (child.children.get(child.children.size() - 1).isNode("InitVal")) {
                        ConstInitVal constInitVal = translateConstInitVal(child.children.get(2));
                        values.add(new GlobalVariable(symbol, constInitVal));
                    } else {
                        values.add(new GlobalVariable(symbol));
                    }
                }
            }
        }

        return values;
    }

    private Function translateMainFuncDef(ASTNode node) {
        Function main = new Function("main", LLVMType.TypeID.IntegerTyID);
        main.setBasicBlock(translateBlock(node.children.get(node.children.size() - 1)));
        return main;
    }

    private Function translateFuncDef(ASTNode node) {
        LeafASTNode ident = (LeafASTNode) node.children.get(1);
        Symbol symbol = getSymbol(ident);
        Function function = new Function(symbol);
        function.setBasicBlock(translateBlock(node.children.get(node.children.size() - 1)));
        return function;
    }

    private BasicBlock translateBlock(ASTNode node) {
        enterScope();
        // System.out.println("In translateBlock: " + node.print());
        BasicBlock block = new BasicBlock();
        for (ASTNode child : node.children) {
            if (child.isNode("BlockItem")) {
                block.addInst(translateBlockItem(child));
            }
        }
        exitScope();
        return new BasicBlock();
    }

    private LinkedList<Instruction> translateBlockItem(ASTNode node) {
        if (node.children.get(0).isNode("Decl")) {
            return translateDecl(node.children.get(0));
        } else {
            return translateStmt(node.children.get(0));
        }
    }

    private LinkedList<Instruction> translateDecl(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateStmt(ASTNode node) {
        LinkedList<Instruction> instructions = new LinkedList<>();
        if (node.children.get(0) instanceof LeafASTNode child) {
            switch (child.token.type) {
                case RETURNTK   -> instructions.addAll(translateReturnStmt(node));
                case IFTK       -> instructions.addAll(translateIfStmt(node));
                case FORTK      -> instructions.addAll(translateForStmt(node));
                case BREAKTK    -> instructions.addAll(translateBreakStmt(node));
                case CONTINUETK -> instructions.addAll(translateContinueStmt(node));
                case PRINTFTK   -> instructions.addAll(translatePrintfStmt(node));
            }
        }

        return instructions;
    }

    private LinkedList<Instruction> translateReturnStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateIfStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateForStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateBreakStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateContinueStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translatePrintfStmt(ASTNode node) {
        return new LinkedList<>();
    }
}
