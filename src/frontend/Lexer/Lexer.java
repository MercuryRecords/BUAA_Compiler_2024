package frontend.Lexer;

import java.io.FileWriter;
import java.io.IOException;
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
        StringBuilder res = new StringBuilder();
        StringBuilder err = new StringBuilder();
        nextToken();
        while (curToken != null) {
            if (curType != LexType.ERROR)
                res.append(curType).append(" ").append(curToken).append("\n");
            else if (!curToken.isEmpty()) {
                err.append(lineNum).append(" a\n");
            }
            nextToken();
        }

        // 将 res 写入 forOutput 文件
        try (FileWriter writer = new FileWriter(forOutput)) {
            writer.write(res.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将 err 写入 forError 文件
        try (FileWriter writer = new FileWriter(forError)) {
            writer.write(err.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextToken() {
        if (curPos >= source.length()) {
            curToken = null;
            return;
        }
        // 更新 curToken 和 curType
        while (source.charAt(curPos) == ' ' || source.charAt(curPos) == '\t' || source.charAt(curPos) == '\n') {
            if (source.charAt(curPos) == '\n') {
                lineNum++;
            }
            curPos++;
        }

        parseSign();
    }

    private void parseSign() {
        switch (source.charAt(curPos)) {
            case '!' -> {
                curPos++;
                if (source.charAt(curPos) == '=') {
                    curToken = "!=";
                    curType = LexType.NEQ;
                    curPos++;
                } else {
                    curToken = "!";
                    curType = LexType.NOT;
                }
            }
            case '<' -> {
                curPos++;
                if (source.charAt(curPos) == '=') {
                    curToken = "<=";
                    curType = LexType.LEQ;
                    curPos++;
                } else {
                    curToken = "<";
                    curType = LexType.LSS;
                }
            }
            case '>' -> {
                curPos++;
                if (source.charAt(curPos) == '=') {
                    curToken = ">=";
                    curType = LexType.GEQ;
                    curPos++;
                } else {
                    curToken = ">";
                    curType = LexType.GRE;
                }
            }
            case '=' -> {
                curPos++;
                if (source.charAt(curPos) == '=') {
                    curToken = "==";
                    curType = LexType.EQL;
                    curPos++;
                } else {
                    curToken = "=";
                    curType = LexType.ASSIGN;
                }
            }
            case '+' -> {
                curToken = "+";
                curType = LexType.PLUS;
                curPos++;
            }
            case '-' -> {
                curToken = "-";
                curType = LexType.MINU;
                curPos++;
            }
            case '*' -> {
                curToken = "*";
                curType = LexType.MULT;
                curPos++;
            }
            case '%' -> {
                curToken = "%";
                curType = LexType.MOD;
                curPos++;
            }
            case ';' -> {
                curToken = ";";
                curType = LexType.SEMICN;
                curPos++;
            }
            case ',' -> {
                curToken = ",";
                curType = LexType.COMMA;
                curPos++;
            }
            case '(' -> {
                curToken = "(";
                curType = LexType.LPARENT;
                curPos++;
            }
            case ')' -> {
                curToken = ")";
                curType = LexType.RPARENT;
                curPos++;
            }
            case '[' -> {
                curToken = "[";
                curType = LexType.LBRACK;
                curPos++;
            }
            case ']' -> {
                curToken = "]";
                curType = LexType.RBRACK;
                curPos++;
            }
            case '{' -> {
                curToken = "{";
                curType = LexType.LBRACE;
                curPos++;
            }
            case '}' -> {
                curToken = "}";
                curType = LexType.RBRACE;
                curPos++;
            }
            case '|' -> {
                curPos++;
                if (source.charAt(curPos) == '|') {
                    curToken = "||";
                    curType = LexType.OR;
                    curPos++;
                } else {
                    curToken = "|";
                    curType = LexType.ERROR;
                }
            }
            case '&' -> {
                curPos++;
                if (source.charAt(curPos) == '&') {
                    curToken = "&&";
                    curType = LexType.AND;
                    curPos++;
                }
                else {
                    curToken = "&";
                    curType = LexType.ERROR;
                }
            }
            default -> {
                curToken = "";
                curType = LexType.ERROR;
                curPos++;
            }
        }
    }
}
