package middleEnd;

import frontEnd.ASTNode;
import frontEnd.LeafASTNode;
import frontEnd.Symbol;
import frontEnd.SymbolTable;
import frontEnd.lexer.LexType;
import middleEnd.Insts.*;
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
    private final HashSet<Integer> usedScopeIds = new HashSet<>();
    private final HashMap<String, Function> functions = new HashMap<>();
    private int scopeId = 0;
    private final ConstCalculator constCalculator;
    private LLVMType.TypeID funcRetType;
    private final Module module = new Module();
    private final RegTracker strNumTracker = new RegTracker();
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
        do {
            scopeId++;
        }
        while (usedScopeIds.contains(scopeId));
        usedScopeIds.add(scopeId);
        LLVMSymbolTable newTable = new LLVMSymbolTable(scopeId, currTable);
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

    private UsableValue getLLVMVariable(String token) {
        LLVMSymbolTable table = currTable;
        while (table != null) {
            if (table.hasVariable(token)) {
                return table.get(token);
            }
            table = table.parentTable;
        }

        throw new RuntimeException("Variable " + token + " not found");
    }

    private void addLLVMVariable(LLVMVariable symbol) {
        newSymbolTables.get(scopeId).addVariable(symbol);
    }

    private void addLLVMFParam(String name, UsableValue value) {
        newSymbolTables.get(scopeId).symbols.put(name, value);
    }

    private Module translateModule(ASTNode root) {
        enterScope();
        for (ASTNode child : root.children) {
            if (child.isNode("Decl")) {
                module.addGlobalDecls(translateGlobalDecl(child));
            } else if (child.isNode("FuncDef")) {
                module.addFunction(translateFuncDef(child));
            } else if (child.isNode("MainFuncDef")) {
                module.addFunction(translateMainFuncDef(child));
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
                    constInitVal = translateConstInitVal(lastChild, arrayLength);
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

    private ConstInitVal translateConstInitVal(ASTNode node, int arrayLength) {
        // 形式上包括常量初值 ConstInitVal 和变量初值 InitVal，但全局变量的初值表达式必须是常量表达式 ConstExp，故按照 ConstInitVal 翻译
        // ConstInitVal  ::= ConstExp | '{' ConstExp { ',' ConstExp } '}' | StringConst
        // InitVal       ::= Exp | '{' Exp { ',' Exp } '}' | StringConst
        // ConstExp      ::= AddExp
        // Exp           ::= AddExp
        if (node.children.get(0).isNode("StringConst")) {
            LeafASTNode leaf = (LeafASTNode) node.children.get(0).children.get(0);
            return new ConstInitVal(leaf.token.token, arrayLength);
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
        funcRetType = main.getReturnType();
        enterScope();
        regTrackers.get(scopeId).nextRegNo();
        Block block = new Block();
        block.addInsts(translateBlock(node.children.get(node.children.size() - 1)));
        main.setBlock(block);
        exitScope();
        return main;
    }

    private Function translateFuncDef(ASTNode node) {
        LeafASTNode ident = (LeafASTNode) node.children.get(1);
        Symbol symbol = getOldSymbol(ident);
        Function function = new Function(symbol);
        functions.put(symbol.token.token, function);
        enterScope();
        funcRetType = function.getReturnType();
        for (ASTNode child : node.children) {
            if (child.isNode("FuncFParams")) {
                LinkedList<FuncFParam> params = translateFuncFParams(child);
                function.setFParams(params);
            }
        }
        regTrackers.get(scopeId).nextRegNo();
        Block block = new Block();
        RegTracker tracker = regTrackers.get(scopeId);
        for (FuncFParam param : function.params) {
            AllocaInst allocaInst = new AllocaInst(tracker.nextRegNo(), param.baseType, 0);
            block.addInst(allocaInst);
            block.addInst(new StoreInst(param, allocaInst));
            addLLVMFParam(param.name, allocaInst);
        }
        block.addInsts(translateBlock(node.children.get(node.children.size() - 1)));
        function.setBlock(block);
        exitScope();
        return function;
    }

    private LinkedList<FuncFParam> translateFuncFParams(ASTNode node) {
        LinkedList<FuncFParam> params = new LinkedList<>();
        for (ASTNode child : node.children) {
            if (child.isNode("FuncFParam")) {
                params.add(translateFuncFParam(child));
            }
        }
        return params;
    }

    private FuncFParam translateFuncFParam(ASTNode node) {
        LeafASTNode ident = (LeafASTNode) node.children.get(1);
        Symbol symbol = getOldSymbol(ident);
        return new FuncFParam(regTrackers.get(scopeId).nextRegNo(), symbol);
    }

    private LinkedList<Instruction> translateBlock(ASTNode node) {
        // System.out.println("In translateBlock: " + node.print());
        LinkedList<Instruction> instructions = new LinkedList<>();
        for (ASTNode child : node.children) {
            if (child.isNode("BlockItem")) {
                instructions.addAll(translateBlockItem(child));
            }
        }
        return instructions;
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
                ConstInitVal constInitVal = translateConstInitVal(lastChild, arrayLength);
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
        for (ASTNode child : node.children) {
            if (child.isNode("VarDef")) {
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
                InitVal initVal = null;
                if (lastChild.isNode("InitVal")) {
                    initVal = translateInitVal(lastChild, arrayLength);
                }
                // 不必填充
                var.setInitVal(initVal);
                addLLVMVariable(var);
                list.addAll(var.getInstructions(regTrackers.get(scopeId)));
            }
        }
        return list;
    }

    private InitVal translateInitVal(ASTNode node, int arrayLength) {
        // InitVal       ::= Exp | '{' Exp { ',' Exp } '}' | StringConst
        if (node.children.get(0).isNode("StringConst")) {
            LeafASTNode leaf = (LeafASTNode) node.children.get(0).children.get(0);
            return new ConstInitVal(leaf.token.token, arrayLength);
        } else {
            InitVal initVal = new InitVal();
            for (ASTNode child : node.children) {
                if (child.isNode("Exp")) {
                    initVal.addExp(translateExp(child));
                }
            }
            return initVal;
        }
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
        } else if (node.children.get(0).isNode("Exp")) {
            instructions.addAll(translateExpStmt(node));
        } else {
            RegTracker tracker = regTrackers.get(scopeId);
            enterScope();
            regTrackers.put(scopeId, tracker);
            instructions.addAll(translateBlock(node.children.get(0)));
            exitScope();
        }

        return instructions;
    }

    private LinkedList<Instruction> translateReturnStmt(ASTNode node) {
        LinkedList<Instruction> instructions = new LinkedList<>();
        if (node.children.size() > 1 && node.children.get(1).isNode("Exp")) {
            LLVMExp exp = translateExp(node.children.get(1));
            instructions.addAll(exp.instructions);
            instructions.add(new RetInst(exp));
        } else {
            instructions.add(new RetInst());
        }
        return instructions;
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
        LinkedList<Instruction> instructions = new LinkedList<>();
        LinkedList<LLVMExp> exps = new LinkedList<>();

        for (ASTNode child : node.children) {
            if (child.isNode("Exp")) {
                LLVMExp exp = translateExp(child);
                instructions.addAll(exp.instructions);
                exps.add(exp);
            }
        }

        LeafASTNode str = (LeafASTNode) node.children.get(2).children.get(0);
        String strVal = str.token.token;
        // 根据 strVal 拆分出常量字符串与函数调用
        StringBuilder tmp = new StringBuilder();
        int tmpLen = 0;
        boolean hasPercent = false;
        boolean hasSlash = false;
        int currExpIndex = 0;
        for (int i = 1; i < strVal.length() - 1; i++) {

            if (hasSlash) {
                if (strVal.charAt(i) == 'n') {
                    tmp.append("\\0A");
                    tmpLen += 1;
                } else {
                    tmp.append('\\');
                    tmp.append(strVal.charAt(i));
                    tmpLen += 2;
                }
                hasSlash = false;
                continue;
            }

            if (hasPercent) {
                hasPercent = false;
                if (strVal.charAt(i) == 'd') {
                    if (!tmp.isEmpty()) {
                        tmp.append("\\00");
                        tmpLen += 1;
                        instructions.addAll(PrintConstStr(tmp.toString(), tmpLen));
                        tmp = new StringBuilder();
                        tmpLen = 0;
                    }

                    LLVMExp exp = exps.get(currExpIndex++);
                    LinkedList<UsableValue> params = new LinkedList<>();
                    if (!exp.toLLVMType().contains("i32")) {
                        ZextInst zextInst = new ZextInst(regTrackers.get(scopeId).nextRegNo(), exp, LLVMType.TypeID.IntegerTyID);
                        instructions.add(zextInst);
                        params.add(zextInst);
                    } else {
                        params.add(exp);
                    }
                    CallInst callInst = new CallInst("putint", params);
                    instructions.add(callInst);
                } else if (strVal.charAt(i) == 'c') {
                    if (!tmp.isEmpty()) {
                        tmp.append("\\00");
                        tmpLen += 1;
                        instructions.addAll(PrintConstStr(tmp.toString(), tmpLen));
                        tmp = new StringBuilder();
                        tmpLen = 0;
                    }

                    LLVMExp exp = exps.get(currExpIndex++);
                    LinkedList<UsableValue> params = new LinkedList<>();
                    if (!exp.toLLVMType().contains("i32")) {
                        ZextInst zextInst = new ZextInst(regTrackers.get(scopeId).nextRegNo(), exp, LLVMType.TypeID.IntegerTyID);
                        instructions.add(zextInst);
                        params.add(zextInst);
                    } else {
                        params.add(exp);
                    }
                    CallInst callInst = new CallInst("putch", params);
                    instructions.add(callInst);
                } else {
                    tmp.append('%');
                    tmp.append(strVal.charAt(i));
                    tmpLen += 2;
                }
                continue;
            }

            if (strVal.charAt(i) == '%') {
                hasPercent = true;
                continue;
            }

            if (strVal.charAt(i) == '\\') {
                hasSlash = true;
                continue;
            }

            tmp.append(strVal.charAt(i));
            tmpLen += 1;

        }

        if (!tmp.isEmpty()) {
            tmp.append("\\00");
            tmpLen += 1;
            instructions.addAll(PrintConstStr(tmp.toString(), tmpLen));
        }

        return instructions;
    }

    private LinkedList<Instruction> PrintConstStr(String string, int tmpLen) {
        LinkedList<Instruction> instructions = new LinkedList<>();
        GlobalString newStr = new GlobalString(strNumTracker.nextRegNo(), string, tmpLen);
        module.addStrDecl(newStr);
        GetelementptrInst getInst = new GetelementptrInst(regTrackers.get(scopeId).nextRegNo(), LLVMType.TypeID.CharTyID, newStr, "0");
        instructions.add(getInst);
        LinkedList<UsableValue> param = new LinkedList<>();
        param.add(getInst);
        CallInst callInst = new CallInst("putstr", param);
        instructions.add(callInst);
        return instructions;
    }

    private LinkedList<Instruction> translateGetCharStmt(ASTNode node) {
        LinkedList<Instruction> instructions = new LinkedList<>();
        UsableValue lval = translateLVal(node.children.get(0));
        if (lval instanceof LLVMExp) {
            instructions.addAll(((LLVMExp) lval).instructions);
        }

        CallInst callInst = new CallInst(regTrackers.get(scopeId).nextRegNo(), LLVMType.TypeID.IntegerTyID, "getchar");
        instructions.add(callInst);
        if (callInst.isDifferentType(lval)) {
            Instruction fix = callInst.fix(regTrackers.get(scopeId).nextRegNo());
            instructions.add(fix);
            instructions.add(new StoreInst((UsableValue) fix, lval));
        } else {
            instructions.add(new StoreInst(callInst, lval));
        }
        return instructions;
    }

    private LinkedList<Instruction> translateGetIntStmt(ASTNode node) {
        LinkedList<Instruction> instructions = new LinkedList<>();
        UsableValue lval = translateLVal(node.children.get(0));
        if (lval instanceof LLVMExp) {
            instructions.addAll(((LLVMExp) lval).instructions);
        }

        CallInst callInst = new CallInst(regTrackers.get(scopeId).nextRegNo(), LLVMType.TypeID.IntegerTyID, "getint");
        instructions.add(callInst);
        if (callInst.isDifferentType(lval)) {
            Instruction fix = callInst.fix(regTrackers.get(scopeId).nextRegNo());
            instructions.add(fix);
            instructions.add(new StoreInst((UsableValue) fix, lval));
        } else {
            instructions.add(new StoreInst(callInst, lval));
        }
        return instructions;
    }

    private LinkedList<Instruction> translateAssignStmt(ASTNode node) {
        LLVMExp exp = translateExp(node.children.get(2));
        LinkedList<Instruction> instructions = new LinkedList<>(exp.instructions);
        UsableValue lval = translateLVal(node.children.get(0));
        if (lval instanceof LLVMExp) {
            instructions.addAll(((LLVMExp) lval).instructions);
        }
        instructions.add(new StoreInst(exp.value, lval));

        return instructions;
    }

    private UsableValue translateLVal(ASTNode node) {
        LeafASTNode leaf = (LeafASTNode) node.children.get(0);
        UsableValue var = getLLVMVariable(leaf.token.token);
        assert var != null;
        if (node.children.size() > 3) {
            LLVMType.TypeID baseType;
            if (var.toLLVMType().contains("i32")) {
                baseType = LLVMType.TypeID.IntegerTyID;
            } else {
                baseType = LLVMType.TypeID.CharTyID;
            }
            LLVMExp exp = translateExp(node.children.get(2));
            String offset = exp.toValueIR();
            if (offset.charAt(0) == '@' || offset.charAt(0) == '%') {
                exp.addUsableInstruction(new ZextInst(regTrackers.get(scopeId).nextRegNo(),exp.value));
                offset = exp.toValueIR();
            }
            GetelementptrInst getInst = new GetelementptrInst(regTrackers.get(scopeId).nextRegNo(), baseType, var, offset);
            exp.addUsableInstruction(getInst);
            return exp;
        } else {
            return var;
        }
    }

    private LinkedList<Instruction> translateExpStmt(ASTNode node) {
        return translateExp(node.children.get(0)).getInstructions();
    }

    public class LLVMExp extends Value implements UsableValue {
        LinkedList<Instruction> instructions;
        UsableValue value;

        public LLVMExp(UsableValue value) {
            this.instructions = new LinkedList<>();
            this.value = value;
        }

        public LLVMExp() {
            this.instructions = new LinkedList<>();
            this.value = null;
        }

        public LLVMExp(LLVMExp exp) {
            this.instructions = new LinkedList<>(exp.instructions);
            this.value = exp.value;
        }

        @Override
        public String toValueIR() {
            return value.toValueIR();
        }

        @Override
        public String toLLVMType() {
            return value.toLLVMType();
        }

        @Override
        public int toAlign() {
            return value.toAlign();
        }

        public LinkedList<Instruction> getInstructions() {
            return instructions;
        }

        public LLVMExp binaryOperate(LLVMType.InstType instType, LLVMExp llvmExp) {
            instructions.addAll(llvmExp.instructions);
            UsableValue left = this.value;
            UsableValue right = llvmExp.value;
            BinaryInst newInst = new BinaryInst(instType, regTrackers.get(scopeId).nextRegNo(), left, right);
            instructions.add(newInst);
            this.value = newInst;
            return this;
        }

        public LLVMExp negate() {
            SubInst newInst = new SubInst(regTrackers.get(scopeId).nextRegNo(), new LLVMConst(LLVMType.TypeID.IntegerTyID, 0), this.value);
            instructions.add(newInst);
            this.value = newInst;
            return this;
        }

        public LLVMExp logicalNot() {
            return this; // TODO
        }

        public void addUsableInstruction(Instruction inst) {
            assert inst instanceof UsableValue;
            instructions.add(inst);
            this.value = (UsableValue) inst;
        }

        public void addFromExp(LLVMExp exp1) {
            instructions.addAll(exp1.instructions);
        }
    }

    private LLVMExp translateExp(ASTNode node) {
        return translateAddExp(node.children.get(0));
    }

    private LLVMExp translateAddExp(ASTNode node) {
        if (node.children.size() == 1) {
            return translateMulExp(node.children.get(0));
        } else {
            LLVMExp left = translateAddExp(node.children.get(0));
            if (((LeafASTNode) node.children.get(1)).token.token.equals("+")){
                return left.binaryOperate(LLVMType.InstType.ADD, translateMulExp(node.children.get(2)));
            } else {
                return left.binaryOperate(LLVMType.InstType.SUB, translateMulExp(node.children.get(2)));
            }
        }
    }

    private LLVMExp translateMulExp(ASTNode node) {
        if (node.children.size() == 1) {
            return translateUnaryExp(node.children.get(0));
        } else {
            LLVMExp left = translateMulExp(node.children.get(0));
            if (((LeafASTNode) node.children.get(1)).token.token.equals("*")) {
                return left.binaryOperate(LLVMType.InstType.MUL, translateUnaryExp(node.children.get(2)));
            } else if (((LeafASTNode) node.children.get(1)).token.token.equals("/")) {
                return left.binaryOperate(LLVMType.InstType.SDIV, translateUnaryExp(node.children.get(2)));
            } else {
                return left.binaryOperate(LLVMType.InstType.SREM, translateUnaryExp(node.children.get(2)));
            }
        }
    }

    private LLVMExp translateUnaryExp(ASTNode node) {
        if (node.children.size() == 1) {
            return translatePrimaryExp(node.children.get(0));
        } else if (node.children.get(0).isNode("UnaryOp")) {
            LeafASTNode leaf = (LeafASTNode) node.children.get(0).children.get(0);
            LLVMExp unaryExp = translateUnaryExp(node.children.get(1));
            return switch (leaf.token.token) {
                case "+" -> unaryExp;
                case "-" -> unaryExp.negate();
                default -> unaryExp.logicalNot();
            };
        } else {
            LLVMExp exp = new LLVMExp();
            LinkedList<LLVMExp> realParams = new LinkedList<>();
            LeafASTNode leaf = (LeafASTNode) node.children.get(0);
            Function toCall = functions.get(leaf.token.token);
            if (node.children.get(2).isNode("FuncRParams")) {
                realParams = new LinkedList<>(translateFuncRParams(node.children.get(2)));
            }
            for (LLVMExp realParam : realParams) {
                exp.addFromExp(realParam);
            }
            LinkedList<UsableValue> forCall = new LinkedList<>();
            for (int i = 0; i < toCall.params.size(); i++) {
                boolean toFix = toCall.params.get(i).isDifferentType(realParams.get(i));
                if (toFix) {
                    Instruction fix = toCall.params.get(i).fix(regTrackers.get(scopeId).nextRegNo(),realParams.get(i));
                    exp.addUsableInstruction(fix);
                    forCall.add((UsableValue) fix);
                } else {
                    forCall.add(realParams.get(i));
                }
            }
            CallInst callInst;
            if (toCall.retType == LLVMType.TypeID.VoidTyID) {
                callInst = new CallInst(toCall.name, forCall);
            } else {
                callInst = new CallInst(regTrackers.get(scopeId).nextRegNo(),
                        toCall.retType, toCall.name, forCall);
            }
            exp.addUsableInstruction(callInst);
            return exp;
        }
    }

    private LinkedList<LLVMExp> translateFuncRParams(ASTNode node) {
        LinkedList<LLVMExp> realParams = new LinkedList<>();
        for (ASTNode child : node.children) {
            if (child.isNode("Exp")) {
                realParams.add(translateExp(child));
            }
        }
        return realParams;
    }

    private LLVMExp translatePrimaryExp(ASTNode node) {
        if (node.children.size() > 1) {
            return translateExp(node.children.get(1));
        } else if (node.children.get(0).isNode("LVal")) {
            return translateLValAsExp(node.children.get(0));
        } else {
            int val;
            String token = ((LeafASTNode) node.children.get(0).children.get(0)).token.token;
            if (node.children.get(0).isNode("Number")) {
                val = Integer.parseInt(token);
            } else {
                val = token.charAt(1);
            }

            LLVMConst number = new LLVMConst(LLVMType.TypeID.IntegerTyID, val);
            return new LLVMExp(number);
        }
    }

    private LLVMExp translateLValAsExp(ASTNode node) {
        UsableValue lVal = translateLVal(node);
        LLVMExp ret;
        if (lVal instanceof LLVMExp exp) {
            ret = new LLVMExp(exp);
        } else {
            ret = new LLVMExp(lVal);
        }
        LLVMType.TypeID baseType;
        if (lVal.toLLVMType().contains("i32")) {
            baseType = LLVMType.TypeID.IntegerTyID;
        } else {
            baseType = LLVMType.TypeID.CharTyID;
        }
        LoadInst loadInst = new LoadInst(regTrackers.get(scopeId).nextRegNo(), baseType, lVal);
        ret.addUsableInstruction(loadInst);
        return ret;
    }
}
