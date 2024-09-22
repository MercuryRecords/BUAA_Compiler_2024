package frontend.Lexer;

import java.util.HashMap;

public class Lexer {
    private final String source;
    private int curPos; // 姑且认为 int 类型足够
    private String curToken;
    private LexType curType;
    private int lineNum;

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

    public Lexer(String source) {
        this.source = source;
        this.curPos = 0;
        this.curToken = null;
        this.curType = null;
        this.lineNum = 1;
    }

    public void analyze(String forOutput, String forError) {
        int len = source.length();
        while (curPos < len) {
            next();
        }
    }

    public void next() {
        curPos++;
    }
}
