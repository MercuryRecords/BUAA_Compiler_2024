package frontend.Lexer;

// 枚举类
/*
    private final HashMap<String, String> reservedWords = new HashMap<String, String>() {
        {
            put("main", "MAINTK");
            put("const", "CONSTTK");
            put("int", "INTTK");
            put("char", "CHARTK");
            put("break", "BREAKTK");
            put("continue", "CONTINUETK");
            put("if", "IFTK");
            put("else", "ELSETK");
            put("for", "FORTK");
            put("getint", "GETINTTK");
            put("getchar", "GETCHARTK");
            put("printf", "PRINTFTK");
            put("return", "RETURNTK");
            put("void", "VOIDTK");
        }
    };
 */
public enum LexType {
    MINU, MULT, MOD, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE, ERROR, NOT, NEQ, LEQ, LSS, GEQ, GRE, EQL, ASSIGN, OR, AND, INTCON, IDENFR, PLUS
    , MAINTK, CONSTTK, INTTK, CHARTK, BREAKTK, CONTINUETK, IFTK, ELSETK, FORTK, GETINTTK, GETCHARTK, PRINTFTK, RETURNTK, STRCON, CHARCON, VOIDTK
}