package frontend;

import java.util.ArrayList;

public class Symbol {
    public int id;
    public int tableId;
    public Token token;
    public BType bType;
    public SymbolType symbolType;
    public ArrayList<Symbol> paras = null;

    public Symbol(int id, int tableId, Token token, BType bType, SymbolType symbolType) {
        this.id = id;
        this.tableId = tableId;
        this.token = token;
        this.bType = bType;
        this.symbolType = symbolType;
    }

    public void setParas(ArrayList<Symbol> paras) {
        this.paras = paras;
    }

    private String printType() {
        // char型常量	ConstChar	    char型变量	    Char	    void型函数	VoidFunc
        // int型常量	    ConstInt	    int型变量	    Int	        char型函数	CharFunc
        // char型常量数组	ConstCharArray	char型变量数组	CharArray	int型函数	IntFunc
        // int型常量数组	ConstIntArray	int型变量数组	    IntArray
        switch (bType) {
            case INT -> {
                switch (symbolType) {
                    case CONST -> {
                        return "ConstInt";
                    }
                    case VAR -> {
                        return "Int";
                    }
                    case FUNC -> {
                        return "IntFunc";
                    }
                    case ARRAY -> {
                        return "IntArray";
                    }
                    case CONSTARRAY -> {
                        return "ConstIntArray";
                    }
                }
            }
            case CHAR -> {
                switch (symbolType) {
                    case CONST -> {
                        return "ConstChar";
                    }
                    case VAR -> {
                        return "Char";
                    }
                    case FUNC -> {
                        return "CharFunc";
                    }
                    case ARRAY -> {
                        return "CharArray";
                    }
                    case CONSTARRAY -> {
                        return "ConstCharArray";
                    }
                }
            }
            case VOID -> {
                switch (symbolType) {
                    case FUNC -> {
                        return "VoidFunc";
                    }
                }
            }
        }
        return "ERROR";
    }

    @Override
    public String toString() {
        return tableId + " " + token.token + " " + printType();
    }
}
