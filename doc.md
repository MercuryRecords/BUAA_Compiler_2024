## 词法分析设计

### 编码前的设计

词法分析部分接受 `testfile.txt` 文件的内容作为字符串输入，需要将字符串解析为一个个词法单元，并按顺序将词法单元内容和类型输出到 `lexer.txt` 文件，将错误信息输出到 `error.txt` 文件。考虑到这样的要求，我将词法分析主要部分设计如下：

```java
public Lexer() {
    // ...
    curToken = null;
    curType = null;
}

public ArrayList<Token> analyze(String forOutput) {
    // ...
    do {
        nextToken();
        if (curType != LexType.NOTE && curToken != null) {
            res.add(new Token(curType, curToken, lineNum));
        }
    } while (curToken != null);
    // ...
}
```

流程确定后，设计的重点来到 `nextToken()` 函数，它需要尝试解析字符串中的下一个词法单元，并更新 `curToken` 和 `curType`。根据实验的词法单元划分，我将词法单元分为以下几类进行分别解析：

- 单个或两个字符的符号，如 `+`, `-`, `*`, `>`, `>=`等；
- 保留字和标识符，如 `int`, `if`, `else`, `for`, `main` 等；
- 数字，如 `123` 等；
- 字符，如 `'a'` 等；
- 字符串，如 `"hello"` 等。

通过这样的划分，我分别设计了五个函数来进行解析，而使用当前字符即可决定使用哪个函数进行解析。至此，词法分析部分的设计已经大体完成。

### 编码完成之后的修改

在进行词法分析设计的时候，我对错误处理的设计并不完善，直到至少语法分析的时候才发现，如果词法分析部分没有处理好错误，那么语法分析部分将无法正常进行。因此，我重新审视了词法分析的错误处理设计并进行了修改。

具体而言，我设计了一个错误处理类 `Reporter`，利用单例模式的设计，使用 `Reporter.REPORTER.add(MyError e)` 和 `Reporter.REPORTER.report()` 即可实现错误信息的存储和输出。因为即使在处理过程中遇到了错误，也需要继续完成词法分析、语法分析和语义分析，因此错误处理部分需要尽量尝试“修复”，而且需要尽量不影响主流程。

```java
private void parseSign() {
    // ...
    switch (source.charAt(curPos)) {
        case '|' -> {
        curPos++;
        if (curPos < source.length() && source.charAt(curPos) == '|') {
            curToken = "||";
            curType = LexType.OR;
            curPos++;
        } else {
            curToken = "|";
            curType = LexType.OR;
            Reporter.REPORTER.add(new MyError(lineNum, "a"));
        }
    }
}
```

## 语法分析

### 编码前的设计

在语法分析阶段，编译器接受词法分析部分的输出 `ArrayList<Token>` 作为输入，并据此构建一个抽象语法树（ASTNode）。为了实现这一目标，我设计了 `Parser` 类，它包含一个 `analyze` 方法，它不仅可以完成上述目标，还需要在递归下降的过程中做另外两件事情：

1. 按词法分析识别单词的顺序，按行输出每个单词的信息。同时，在文法中出现指定语法分析成分分析结束前，需要另起一行输出当前语法成分的名字，形如“<Stmt>”。以上述输出信息需要输出到 `parser.txt` 文件中；
2. 在语法分析过程中，如果遇到错误，需要输出错误信息。错误信息需要输出到 `error.txt` 文件中。

因此，`Parser` 类的设计如下：

```java
public class Parser {
    private final ArrayList<Token> tokens;
    private int index = 0;
    private final StringBuilder sb = new StringBuilder();
    private final boolean OUTPUT = true;
    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode analyze(String forOutput) {
        ASTNode root = parseCompUnit();
        try (FileWriter writer = new FileWriter(forOutput)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return root;
    }
}
```

从上述代码中可以看出，`analyze` 方法接受一个字符串作为参数，用于指定输出文件名。它首先调用 `parseCompUnit()` 方法进行语法分析，并返回根节点 `ASTNode`。然后，它将 `StringBuilder` 中的内容写入到指定的输出文件中。在进行递归下降的过程中，`StringBuilder` 会被不断更新，以记录语法分析过程中的输出信息。就递归下降部分而言，我为所有语法成分都设计了对应的解析函数，如 `parseCompUnit()`、`parseDecl()`、`parseStmt()` 等。这些函数会根据当前词法单元的类型和文法规则，递归地调用其他解析函数，以完成语法分析。几个典型的解析函数如下：

```java
private ASTNode parseCompUnit() {
    ASTNode node = new ASTNode("CompUnit");
    while (isDecl()) {
        node.addChild(parseDecl());
    }
    while (isFuncDef()) {
        node.addChild(parseFuncDef());
    }
    node.addChild(parseMainFuncDef());
    return node;
}

private ASTNode parseForStmt() {
    // <ForStmt> ::=  <LVal> '=' <Exp>
    ASTNode node = new ASTNode("ForStmt");
    node.addChild(parseLVal());
    node.addChild(parseTokenType(LexType.ASSIGN));
    node.addChild(parseExp());

    if (OUTPUT) {
        sb.append(node.print()).append("\n");
    }
    return node;
}
```

可以看到为了适应输出语法成分的要求，我在每个解析函数中都添加了 `if (OUTPUT)` 的判断，以决定是否输出语法成分的信息。在 `ASTNode` 类中，我设计了 `print()` 方法，用于输出语法成分的信息。

```java
public class ASTNode {
    public String name;
    public ArrayList<ASTNode> children;

    public ASTNode(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public String print() {
        if (this instanceof LeafASTNode) return "";
        return "<" + name + ">";
    }
}
```

对于输出单词信息的要求，我在 `Parser` 类设计了如下方法：

```java
private ASTNode parseTokenType(LexType type) {
        if (!curToken().isType(type))
            return null;
    if (OUTPUT)
        sb.append(curToken()).append("\n");
    nextToken();
    return new LeafASTNode(tokenWithOffset(-1));
}
```

这个方法在语法分析的过程中被频繁使用。它接受一个 `LexType` 类型的参数，用于判断当前词法单元是否为指定的类型。如果是，则输出该词法单元的信息，并返回一个 `LeafASTNode` 类型的节点，表示该词法单元。否则，返回 `null`。

### 编码完成之后的修改

在编码完成之后，我主要进行了一些 debug 的工作，包括：

1. 我为了递归下降分析的方便，对一部分文法进行了自定义的改动。如` AddExp → MulExp | AddExp ('+' | '−') MulExp ` 被我无意识地改写成了错误的 EBNF 格式，即 `AddExp → MulExp {('+' | '−') MulExp} `。这导致递归下降分析完成之后，我建立的语法树结构与文法要求的结构不符。因此，我对类似的递归下降分析函数进行了修改，以使其能够正确地构建语法树。
2. 在错误处理部分，我一开始的实现能够正确发现错误，但是对于已经发现的错误，我使用了一个自定义的 `CompilerException` 类，它继承自 `Exception`，用于抛出错误。然而，我初版的处理是发现错误之后，抛出异常，然后终止程序。这导致我无法在错误发生之后继续进行语法分析。因此，我对错误处理部分进行了修改，将 `CompilerException` 类改为 `MyError` 类，并不再从 `Exception` 继承，而是直接在发现错误时使用自定义的错误处理类 `Reporter` 添加错误信息。这样修改之后，我就可以在错误发生之后继续进行语法分析，直到分析完成，满足输出和分析要求。

## 语义分析

### 编码前的设计

在本课程的实验设计中，语义分析实验要求我们实现以下功能：

1. 对于正确的源程序，需要从源程序中识别出定义的常量、变量、函数、形参，输出它们的作用域序号，单词的字符/字符串形式，类型名称。
2. 对于错误的源程序，需要识别出错误，并输出错误所在的行号和错误的类别码。

为了实现以上功能，我设计了 `Visitor` 类，定义了访问不同类型节点的方法，如 `visitConstDecl`、`visitVarDecl`、`visitFuncDef` 等。通过递归地调用这些方法，编译器可以在遍历语法树的过程中，对每个节点进行语义分析。 `Visitor` 类包含一个 analyze 方法，该方法接受一个 `ASTNode` 对象作为输入，并返回一个 `HashMap<Integer, SymbolTable>` 作为输出，供后续中间代码生成部分使用。

#### 符号表管理

语义分析过程中需要维护符号表，用于记录变量的定义和作用域。`Visitor` 类中使用 `SymbolTable` 类来表示符号表，并使用`symbolTableStack` 来使用栈式地管理符号表。得益于栈式管理，符号表可以很好地支持嵌套作用域。在每个作用域开始时，编译器会创建一个新的符号表，并将其压入栈中。在作用域结束时，编译器会弹出栈顶的符号表。这样，编译器就可以在遍历语法树的过程中，正确地维护符号表的状态。

#### 错误处理

在语义分析的过程中，编译器需要关注文法中剩下的所有未处理的可能错误，包括变量未定义和变量重定义等。对于变量未定义，编译器会在访问到相应的节点时，检查符号表中是否存在该变量；对于变量重定义，编译器会在访问到相应的节点时，检查符号表中是否已经存在该变量。对于这两个比较频繁的检查操作，我将其分别封装成了两个方法：

```java
private boolean checkErrorB(Token token) {
    if (currTable.hasSymbol(token.token)) {
        Reporter.REPORTER.add(new MyError(token.lineNum, "b"));
        return false;
    }
    return true;
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
```

对于其他的错误，如类型不匹配等，我则直接在相应的节点访问方法中进行处理。

#### 输出信息

在语义分析的过程中，编译器需要输出一些信息，包括变量的作用域序号、单词的字符/字符串形式、类型名称等。我选择在遍历完语法树之后，将所有的符号信息输出到目标文件中。由于实验对输出格式的要求，我对符号表中的符号进行了排序，并按照要求输出。具体实现如下：

```java
public HashMap<Integer, SymbolTable> analyze(String forOutput) {
    visitCompUnit(root);
    ArrayList<Symbol> symbols = new ArrayList<>();
    for (SymbolTable table : allSymbolTables.values()) {
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
    return allSymbolTables;
}
```

当然，在 `Symbol` 类中，我重写了 `toString` 方法，以便于输出。

```java
public class Symbol {
    public int id;
    public int tableId;
    public Token token;
    public SymbolType symbolType;
    public ArrayList<Symbol> params = new ArrayList<>();

    // 其他方法

    @Override
    public String toString() {
        return tableId + " " + token.token + " " + symbolType;
    }
}
```

### 编码完成之后的修改

在初步编码完成之后，我主要发现了这个问题：由于我为所有语法树结点都统一使用 `ASTNode` 类，而没有为每个语法成分实现特定的类，因此在语义分析的过程中，我需要直接对 `ASTNode` 对象 `children` 字段取下标以获取子节点。然而，由于输入的 `testfile.txt` 文件中可能有错误，即某些语法成分可能不存在于之前生成的语法树中，因此我需要对下标的选取进行比较细致的考量，不能直接使用特定的下标，而是需要使用循环遍历 `children` 字段，以确保能够正确地获取到子节点。

```java
// 原有代码
private void visitConstDef(ASTNode node, _SymbolType1 symbolType1) {
    // <ConstDef> ::= <Ident> '=' <ConstInitVal> | <Ident> '[' <ConstExp> ']' '=' <ConstInitVal>
    Token token = ((LeafASTNode) node.children.get(0)).token;
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

// 修改后的代码
private void visitConstDef(ASTNode node, _SymbolType1 symbolType1) {
    // <ConstDef> ::= <Ident> '=' <ConstInitVal> | <Ident> '[' <ConstExp> ']' '=' <ConstInitVal>
    Token token = ((LeafASTNode) node.children.get(0)).token;
    checkErrorB(token);
    _SymbolType2 symbolType2 = _SymbolType2.CONST;
    for (ASTNode child : node.children) {
        if (child.isNode("LEAF")) {
            if (((LeafASTNode) child).token.isType(LexType.LBRACK)) {
                symbolType2 = _SymbolType2.CONSTARRAY;
            }
        }
    }
    currTable.addSymbol(new Symbol(symbolId++, currTable.id, token, symbolType1, symbolType2));
}
```

在后续的实验中，我逐渐意识到，为所有语法树结点都统一使用 `ASTNode` 类的做法虽然能减轻工作量，但并不利于后续的语义分析。如果为每个语法成分实现特定的类，那么在语义分析和中间代码生成的过程中，我就可以直接使用这些特定的类，而不需要使用循环遍历 `children` 字段。

## 中间代码生成

### 编码前的设计

本次实验要求根据文法规则及语义约定，采用自顶向下的语法制导翻译技术，进行语义分析并生成目标代码。我首先选择了生成 LLVM IR 代码，因为 LLVM IR 是一种中间表示，能够很好地描述程序的控制流和数据的表示。同时，由于本系列实验的文法实际上是 C 语言的一个子集，因此在配置好编译环境后，可以使用 Clang 的编译器生成标准的 LLVM IR 代码，从而验证我的代码的正确性。

要翻译出 LLVM IR 代码，首先需要对其有一定的了解。我首先花了相当多的时间来理解 LLVM IR 中“一切皆 Value ”的思想，以及如何使用 LLVM IR 来描述程序的控制流和数据流。在了解了 LLVM IR 的基本概念之后，就可以开始编写代码了。首先需要意识到，在中间代码生成的过程中，总可以将一个语法成分翻译为一或多个 LLVM IR 语句，或者翻译为一个 LLVM IR 表达式。因此，在实现中间代码生成时，我选择继续使用自顶向下的语法制导翻译技术，为每个语法成分实现对应的方法，并在这些方法中生成相应的 LLVM IR 代码。

除了教程中已经涉及的部分，我对 LLVM IR 的翻译还有一些理解与实现，说明如下：

#### 全局变量

在 LLVM IR 中，全局变量需要使用 `@` 符号开头，并且需要指定类型。由于全局变量在程序运行期间一直存在，因此需要使用 `global` 关键字。此外，全局变量还需要指定初始值，可以使用 `=` 符号来指定初始值。在实验文法中有这样的要求：“对于全局变量中的常量表达式，在生成的 LLVM IR 中需要直接算出其具体的值，同时，也需要完成必要的类型转换。”因此，在生成全局变量的 LLVM IR 代码时，我实现了一个工具类 `ConstCalculator` ，用于计算常量表达式的值，并在生成全局变量的 LLVM IR 代码时调用该工具类。

```java
public class ConstCalculator {
    private final HashMap<Integer, LLVMSymbolTable> symbolTables;
    private int scopeId; // 查找用

    public ConstCalculator(HashMap<Integer, LLVMSymbolTable> symbolTables) {
        this.symbolTables = symbolTables;
    }

    private int getConst(String name, int i) {
        int currScopeId = scopeId;
        while (currScopeId > 0) {
            if (symbolTables.get(currScopeId).symbols.containsKey(name)) {
                UsableValue usableValue = symbolTables.get(currScopeId).symbols.get(name);
                ConstInitVal constInitVal = symbolTables.get(currScopeId).getConstInitVal(usableValue);
                return constInitVal.getConstValue(i);
            }

            if (symbolTables.get(currScopeId).parentTable == null) {
                break;
            }

            currScopeId = symbolTables.get(currScopeId).parentTable.id;
        }
        throw new RuntimeException("Const not found: " + name);
    }

    public int calculateConstExp(ASTNode node, int scopeId) {
        this.scopeId = scopeId;
        return calculateConstExp(node);
    }
    // ...
}
```

#### 整体流程

考虑完相对独立的全局变量翻译，我接着从整体考虑如何利用对 `toString` 方法的重写生成 LLVM IR 代码。在编写 LLVM IR 各成分对应的类时，我吸取了语法分析阶段的经验，为每个语法成分都实现了对应的类，并在这些类中重写了 `toString` 方法，从而在生成 LLVM IR 代码时，能够直接调用这些类的 `toString` 方法来生成相应的 LLVM IR 代码。

```java
public class LLVMModule {
    // <CompUnit> ::= {<Decl>} {<FuncDef>} <MainFuncDef>
    public final LinkedList<Value> globalValues = new LinkedList<>();
    public final LinkedList<LLVMFunction> LLVMFunctions = new LinkedList<>();

    // ...

    @Override
    public String toString() {
        StringBuilder module = new StringBuilder();
        for (Value value : globalValues) {
            module.append(value.toString()).append("\n");
        }
        for (LLVMFunction LLVMFunction : LLVMFunctions) {
            module.append(LLVMFunction.toString()).append("\n");
        }
        return module.toString();
    }
}
```

#### 局部变量

由于局部变量在函数调用时才分配内存，因此需要使用 alloca 指令来分配内存。在这一部分，我部分地利用了语法分析阶段生成的符号表，通过查询符号表，可以知道局部变量的类型，从而生成相应的 alloca 指令。与此同时，我也根据 LLVM IR 的性质管理了适用于 LLVM IR 的符号表，从而在生成 LLVM IR 代码时，能够正确地引用局部变量。

```java
public class LLVMVariable extends Value {
    public boolean isConst;
    public String name;
    public int arrayLength; // 为 0 是表示不是数组
    public LLVMType.TypeID baseType;
    public InitVal initVal;
    public UsableValue usableValue;

    public LLVMVariable(Symbol symbol, int arrayLength) {
        super();
        setFromSymbol(symbol);
        this.arrayLength = arrayLength;
    }

    public LinkedList<LLVMInstruction> getInstructions() {
        LinkedList<LLVMInstruction> instructions = new LinkedList<>();
        if (arrayLength == 0) {
            // 单个变量/常量
            if (isConst) {
                // 单个常量
                // ...
            } else {
                // 单个变量
                // ...
            }
        } else {
            // 数组
            if (isConst || initVal instanceof ConstInitVal) {
                // 常量数组
                // ...
            } else {
                // 变量数组
                // ...
            }
        }
        return instructions;
    }
}
```

#### 表达式与虚拟寄存器

在 LLVM IR 中，表达式计算的结果通常存储在虚拟寄存器中，因此需要为每个虚拟寄存器生成一个唯一的标识符。为了满足虚拟表达式的需求，我定义了一个 `UsableValue` 接口，用于表示可用的值，包括常量、变量和虚拟寄存器。在生成 LLVM IR 代码时，需要根据表达式的类型生成相应的虚拟寄存器、正确地引用这些虚拟寄存器。

同时，除了一些可以直接解析为 LLVM IR 语法成分类的语法树结点外，还有些语法树结点通过解析生成的是一系列 LLVM IR 指令，即 `LinkedList<LLVMInstruction>` 。在解析 `Exp` 结点时，我意识到不仅需要返回一个 `UsableValue` ，还需要返回一个 `LinkedList<LLVMInstruction>` ，用于生成 LLVM IR 代码。因此，我创建了一个 `LLVMExp` 类，用于表示可用的值和对应的 LLVM IR 指令。通过使用这个类的一些方法，可以方便地生成 LLVM IR 代码。

```java
public class LLVMExp extends Value {
    LinkedList<LLVMInstruction> instructions = new LinkedList<>();
    UsableValue value;

    // ...

    public LLVMExp binaryOperate(LLVMType.InstType instType, LLVMExp llvmExp) {
        instructions.addAll(llvmExp.instructions);
        UsableValue left = this.value;
        UsableValue right = llvmExp.value;
        BinaryInst newInst = new BinaryInst(instType, left, right);
        instructions.add(newInst);
        this.value = newInst;
        return this;
    }

    public void logical() {
        BinaryInst newInst = new BinaryInst(LLVMType.InstType.ICMP_NE, this.value, new LLVMConst(LLVMType.TypeID.IntegerTyID, 0));
        instructions.add(newInst);
        this.value = newInst;
    }
}
```

#### 条件与循环

在生成这部分的 LLVM IR 代码时，代码需要跳转到之后还未构建的基本块，这基本也决定了如果采用数字编号虚拟寄存器的方式，那么很难实现跳转的功能。因此，我仿照 LLVM IR 中的 `SlotTracker`，使用 `LLVMLabel` 来标记基本块，并实现了一个简易的 `RegTracker` 类来管理虚拟寄存器，在生成需要用到虚拟寄存器的指令时，通过 `RegTracker` 进行记录；在完成翻译工作后，通过 `RegTracker` 生成对应的虚拟寄存器编号。这个类还可以很好地与我的 `UsableValue` 接口配合使用，从而实现虚拟寄存器的引用。

```java
public class RegTracker {
    private final int scopeId;
    private final LinkedList<UsableValue> usableValues = new LinkedList<>();
    private int regNo = 0; // 已经用过的编号

    public RegTracker(int scopeId) {
        this.scopeId = scopeId;
    }

    public void addValue(UsableValue inst) {
        usableValues.add(inst);
    }

    public void setRegNo() {
        boolean FParamsIsEnd = false;
        for (UsableValue value : usableValues) {
            if (!(value instanceof FuncFParam) && !FParamsIsEnd) {
                FParamsIsEnd = true;
                regNo++;
            }
            if (value instanceof CallInst callInst) {
                if (callInst.isVoid()) {
                    continue;
                }
            }
            value.setVirtualRegNo(regNo++);
        }
    }
}
```

### 编码完成之后的修改

在初步编写中间代码生成部分时，我对 LLVM IR 的语法并不是很熟悉，因此有些地方写的不符合规范。通过对照公共测试库的标准答案，伴随着不断地修改和调试，我逐渐熟悉了 LLVM IR 的语法，并按照规范编写了代码。在此部分能说明的修改数不胜数，因此我不再一一列举。重点在于，要习惯于利用 Clang 的 `clang` 命令行工具来生成标准的 LLVM IR 代码，通过对比自己的代码和标准答案，可以快速发现并修改错误。中间代码生成部分体量庞大，在思考和编写代码上花费的时间也较多，因此我建议后来人在编写代码时，要尽量单元化地编写代码，并对新增的功能进行充分的测试，以减少调试和修改代码的时间；在编写编译器代码时也要多从全局思考，对编译器的各个部分进行合理的分工，适时进行代码重构，提高代码的可维护性。