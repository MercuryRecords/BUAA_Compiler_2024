package middleEnd.utils;

import frontEnd.ASTNode;
import frontEnd.LeafASTNode;
import frontEnd.Token;
import frontEnd.lexer.LexType;
import middleEnd.ConstInitVal;
import middleEnd.LLVMSymbolTable;
import middleEnd.LLVMVariable;

import java.util.HashMap;

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
                ConstInitVal constInitVal = (ConstInitVal) ((LLVMVariable) symbolTables.get(currScopeId).symbols.get(name)).initVal;
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

    private int calculateConstExp(ASTNode node) {
        return calculateAddExp(node.children.get(0));
    }

    private int calculateAddExp(ASTNode node) {
        if (node.children.size() == 1) {
            return calculateMulExp(node.children.get(0));
        } else {
            int left = calculateAddExp(node.children.get(0));
            int right = calculateMulExp(node.children.get(2));
            if (((LeafASTNode) node.children.get(1)).token.token.equals("+")) {
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
            if (((LeafASTNode) node.children.get(1)).token.token.equals("*")) {
                return left * right;
            } else if (((LeafASTNode) node.children.get(1)).token.token.equals("/")) {
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
            ASTNode lvalNode = node.children.get(0);
            if (lvalNode.children.size() == 1) {
                return getConst(token, 0);
            } else {
                return getConst(token, calculateConstExp(lvalNode.children.get(2)));
            }
        } else {
            LeafASTNode leaf = (LeafASTNode) node.children.get(0).children.get(0);
            Token token = leaf.token;
            if (token.isType(LexType.INTCON)) {
                return Integer.parseInt(token.token);
            } else {
                return token.token.charAt(1);
            }
        }
    }
}
