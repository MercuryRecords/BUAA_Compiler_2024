package middleEnd;

import frontEnd.ASTNode;
import frontEnd.LeafASTNode;
import frontEnd.Symbol;
import frontEnd.SymbolTable;
import frontEnd.lexer.LexType;
import middleEnd.utils.ConstCalculator;
import middleEnd.utils.RegTracker;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IRGenerator {
    private final ASTNode root;
    private final HashMap<Integer, SymbolTable> oldSymbolTables; // 仅用于快捷查询符号类型，LLVM IR 生成过程中需要新符号表
    private LLVMSymbolTable currTable = null;
    private final Stack<LLVMSymbolTable> symbolTableStack = new Stack<>();
    private final HashMap<Integer, LLVMSymbolTable> newSymbolTables = new HashMap<>();
    private final HashMap<Integer, RegTracker> regTrackers = new HashMap<>();
    private int scopeId = 0;
    private final ConstCalculator constCalculator;
    public IRGenerator(ASTNode root, HashMap<Integer, SymbolTable> oldSymbolTables) {
        this.root = root;
        this.oldSymbolTables = oldSymbolTables;
        this.constCalculator = new ConstCalculator(newSymbolTables);
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
        LLVMSymbolTable newTable = new LLVMSymbolTable(++scopeId, currTable);
        RegTracker tracker = new RegTracker(scopeId);
        symbolTableStack.push(newTable);
        newSymbolTables.put(newTable.id, newTable);
        regTrackers.put(scopeId, tracker);
        currTable = newTable;
    }

    private void exitScope() {
        symbolTableStack.pop();
        try {
            currTable = symbolTableStack.peek();
            scopeId = currTable.id;
        } catch (EmptyStackException e) {
            currTable = null;
            scopeId = 0;
        }
    }

    private Symbol getOldSymbol(LeafASTNode node) {
        String identName = node.token.token;
        return oldSymbolTables.get(scopeId).getSymbol(identName);
    }

    private void addLLVMVariable(LLVMVariable symbol) {
        newSymbolTables.get(scopeId).addVariable(symbol);
    }

    private Module translateModule(ASTNode root) {
        Module module = new Module();
        ArrayList<ASTNode> children = root.children;
        enterScope();
        for (ASTNode child : children) {
            if (child.isNode("Decl")) {
                module.addGlobalValues(translateGlobalDecl(child));
            } else if (child.isNode("FuncDef")) {
                module.addGlobalValue(translateFuncDef(child));
            } else if (child.isNode("MainFuncDef")) {
                module.addGlobalValue(translateMainFuncDef(child));
            }
        }
        exitScope();

        return module;
    }

    private LinkedList<Value> translateGlobalDecl(ASTNode node) {
        node = node.children.get(0);
        LinkedList<Value> values = new LinkedList<>();
        for (ASTNode child : node.children) {
            if (child.isNode("ConstDef") || child.isNode("VarDef")) {
                LeafASTNode ident = (LeafASTNode) child.children.get(0);
                Symbol symbol = getOldSymbol(ident);
                GlobalVariable var;
                int arrayLength;
                if (symbol.symbolType.toString().endsWith("Array")) {
                    arrayLength = calculateConstExp(child.children.get(2));
                } else {
                    arrayLength = 0;
                }
                var = new GlobalVariable(symbol, arrayLength);
                ASTNode lastChild = child.children.get(child.children.size() - 1);
                ConstInitVal constInitVal;
                if (lastChild.isNode("InitVal") || lastChild.isNode("ConstInitVal")) {
                    constInitVal = translateConstInitVal(lastChild);
                } else {
                    constInitVal = new ConstInitVal();
                }
                constInitVal.padToLength(arrayLength);
                var.setInitVal(constInitVal);
                values.add(var);
                addLLVMVariable(var);
            }
        }
        return values;
    }

    private ConstInitVal translateConstInitVal(ASTNode node) {
        // 形式上包括常量初值 ConstInitVal 和变量初值 InitVal，但全局变量的初值表达式必须是常量表达式 ConstExp，故按照 ConstInitVal 翻译
        // ConstInitVal  ::= ConstExp | '{' ConstExp { ',' ConstExp } '}' | StringConst
        // InitVal       ::= Exp | '{' Exp { ',' Exp } '}' | StringConst
        // ConstExp      ::= AddExp
        // Exp           ::= AddExp
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

    private int calculateConstExp(ASTNode node) {
        // ConstExp      ::= AddExp，但文法规定使用的 Ident 必须是常量，故可以计算得出
        return constCalculator.calculateConstExp(node, scopeId);
    }

    private Function translateMainFuncDef(ASTNode node) {
        Function main = new Function("main", LLVMType.TypeID.IntegerTyID);
        enterScope();
        main.setBlock(translateBlock(node.children.get(node.children.size() - 1)));
        exitScope();
        return main;
    }

    private Function translateFuncDef(ASTNode node) {
        LeafASTNode ident = (LeafASTNode) node.children.get(1);
        Symbol symbol = getOldSymbol(ident);
        Function function = new Function(symbol);
        enterScope();
        for (ASTNode child : node.children) {
            if (child.isNode("FuncFParams")) {
                // TODO 形参
                function.setFParams();
            }
        }
        function.setBlock(translateBlock(node.children.get(node.children.size() - 1)));
        exitScope();
        return function;
    }

    private Block translateBlock(ASTNode node) {
        // System.out.println("In translateBlock: " + node.print());
        Block block = new Block();
        for (ASTNode child : node.children) {
            if (child.isNode("BlockItem")) {
                block.addInst(translateBlockItem(child));
            }
        }
        return block;
    }

    private LinkedList<Instruction> translateBlockItem(ASTNode node) {
        if (node.children.get(0).isNode("Decl")) {
            return translateDecl(node.children.get(0));
        } else {
            return translateStmt(node.children.get(0));
        }
    }

    private LinkedList<Instruction> translateDecl(ASTNode node) {
        // <Decl> ::= <ConstDecl> | <VarDecl>
        LinkedList<Instruction> instructions = new LinkedList<>();
        if (node.children.get(0).isNode("ConstDecl")) {
            instructions.addAll(translateConstDecl(node.children.get(0)));
        } else {
            instructions.addAll(translateVarDecl(node.children.get(0)));
        }
        return instructions;
    }

    private LinkedList<Instruction> translateConstDecl(ASTNode node) {
        LinkedList<Instruction> list = new LinkedList<>();
        for (ASTNode child : node.children) {
            if (child.isNode("ConstDef") ) {
                LeafASTNode ident = (LeafASTNode) child.children.get(0);
                Symbol symbol = getOldSymbol(ident);
                LLVMVariable var;
                int arrayLength;
                if (symbol.symbolType.toString().endsWith("Array")) {
                    arrayLength = calculateConstExp(child.children.get(2));
                } else {
                    arrayLength = 0;
                }
                var = new LLVMVariable(symbol, arrayLength);
                ASTNode lastChild = child.children.get(child.children.size() - 1);
                ConstInitVal constInitVal = translateConstInitVal(lastChild);
                constInitVal.padToLength(arrayLength);
                var.setInitVal(constInitVal);
                addLLVMVariable(var);
                list.addAll(var.getInstructions(regTrackers.get(scopeId)));
            }
        }
        return list;
    }

    private LinkedList<Instruction> translateVarDecl(ASTNode node) {
        LinkedList<Instruction> list = new LinkedList<>();

        return list;
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
        } else if (node.children.get(0).isNode("LVal")) {
            if (node.children.size() > 2 && node.children.get(2).isNode("LEAF")) {
                LexType type = ((LeafASTNode) node.children.get(2)).token.type;
                if (type == LexType.GETCHARTK) {
                    instructions.addAll(translateGetCharStmt(node));
                } else {
                    instructions.addAll(translateGetIntStmt(node));
                }
            } else {
                instructions.addAll(translateAssignStmt(node));
            }
        } else {
            instructions.addAll(translateExpStmt(node));
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

    private LinkedList<Instruction> translateGetCharStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateGetIntStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateAssignStmt(ASTNode node) {
        return new LinkedList<>();
    }

    private LinkedList<Instruction> translateExpStmt(ASTNode node) {
        return new LinkedList<>();
    }
}
