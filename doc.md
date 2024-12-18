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
```

```java
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

