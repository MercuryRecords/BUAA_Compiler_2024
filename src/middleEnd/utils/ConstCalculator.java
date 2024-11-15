package middleEnd.utils;

import frontEnd.ASTNode;
import frontEnd.LeafASTNode;
import frontEnd.SymbolTable;
import frontEnd.Token;
import frontEnd.lexer.LexType;
import middleEnd.GlobalVariable;

import java.util.HashMap;

public class ConstCalculator {
    private final HashMap<Integer, SymbolTable> symbolTables;
    private final HashMap<Integer, HashMap<String, GlobalVariable>> constMap = new HashMap<>();
    private int scopeId; // 查找用
    public ConstCalculator(HashMap<Integer, SymbolTable> symbolTables) {
        this.symbolTables = symbolTables;
    }

    private void checkSet(int scopeId) {
        if (!constMap.containsKey(scopeId)) {
            constMap.put(scopeId, new HashMap<>());
        }
    }


    public void add(int scopeId, GlobalVariable var) {
        checkSet(scopeId);
        constMap.get(scopeId).put(var.name, var);
    }

    private int getConst(String name) {
        int currScopeId = scopeId;
        while (currScopeId > 0) {
            checkSet(scopeId);
            if (constMap.get(currScopeId).containsKey(name)) {
                return constMap.get(currScopeId).get(name).getConstValue();
            }

            SymbolTable table = symbolTables.get(currScopeId).parentTable;
            if (table == null) {
                break;
            }
            currScopeId = table.id;
        }

        throw new RuntimeException("Const not found: " + name);
    }

    private int getConst(String name, int i) {
        int currScopeId = scopeId;
        while (currScopeId > 0) {
            checkSet(scopeId);
            if (constMap.get(currScopeId).containsKey(name)) {
                return constMap.get(currScopeId).get(name).getConstValue(i);
            }

            SymbolTable table = symbolTables.get(currScopeId).parentTable;
            if (table == null) {
                break;
            }
            currScopeId = table.id;
        }

        throw new RuntimeException("Const not found: " + name);
    }

    public int calculateConstExp(int scopeId, ASTNode node) {
        this.scopeId = scopeId;
        return calculateConstExp(node);
    }

    private int calculateConstExp(ASTNode node) {
        return calculateAddExp(node.children.get(0));
    }

    private int calculateAddExp(ASTNode node) {
        if (node.children.size() == 1) {
            return calculateMulExp(node.children.get(0));
        } else {
            int left = calculateAddExp(node.children.get(0));
            int right = calculateMulExp(node.children.get(2));
            if (node.children.get(1).name.equals("+")) {
                return left + right;
            } else {
                return left - right;
            }
        }
    }

    private int calculateMulExp(ASTNode node) {
        if (node.children.size() == 1) {
            return calculateUnaryExp(node.children.get(0));
        } else {
            int left = calculateMulExp(node.children.get(0));
            int right = calculateUnaryExp(node.children.get(2));
            if (node.children.get(1).name.equals("*")) {
                return left * right;
            } else if (node.children.get(1).name.equals("/")) {
                return left / right;
            } else {
                return left % right;
            }
        }
    }

    private int calculateUnaryExp(ASTNode node) {
        if (node.children.size() == 1) {
            return calculatePrimaryExp(node.children.get(0));
        } else if (node.children.get(0).name.equals("LEAF")) {
            LeafASTNode leaf = (LeafASTNode) node.children.get(0);
            String token = leaf.token.token;
            // 那就不是常量了啊
            throw new RuntimeException("In ConstCalculator, Found UnaryExp ::= Ident '(' ... ')' : " + token);
        } else {
            ASTNode unaryOpNode = node.children.get(0);
            LeafASTNode leaf = (LeafASTNode) unaryOpNode.children.get(0);
            String token = leaf.token.token;
            if (token.equals("+")) {
                return calculateUnaryExp(node.children.get(1));
            } else if (token.equals("-")) {
                return -1 * calculateUnaryExp(node.children.get(1));
            } else {
                throw new RuntimeException("In ConstCalculator, Found UnaryOp ::= '!'");
            }
        }
    }

    private int calculatePrimaryExp(ASTNode node) {
        if (node.children.size() == 3) {
            return calculateConstExp(node.children.get(1));
        } else if (node.children.get(0).isNode("LVal")) {
            LeafASTNode leaf = (LeafASTNode) node.children.get(0).children.get(0);
            String token = leaf.token.token;
            if (node.children.size() == 1) {
                return getConst(token);
            } else {
                return getConst(token, calculateConstExp(node.children.get(2)));
            }
        } else {
            LeafASTNode leaf = (LeafASTNode) node.children.get(0).children.get(0);
            Token token = leaf.token;
            if (token.isType(LexType.INTCON)) {
                return Integer.parseInt(token.token);
            } else {
                return token.token.charAt(0);
            }

        }
    }
}
