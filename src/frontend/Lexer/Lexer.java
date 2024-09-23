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
    private final HashMap<String, String> reservedWords = new HashMap<>() {
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
        if (Character.isDigit(source.charAt(curPos))) {
            parseNumber();
        } else if (isIdentifierFirstLetter(source.charAt(curPos))) {
            parseIdentifierOrReservedWord();
        } else if (source.charAt(curPos) == '\"') {
            parseString();
        } else if (source.charAt(curPos) == '\'') {
            parseChar();
        } else {
            parseSign();
        }
    }

    private void parseNumber() {
        StringBuilder sb = new StringBuilder();
        if (source.charAt(curPos) == '0') {
            sb.append(source.charAt(curPos));
            curPos++;
        } else {
            while (Character.isDigit(source.charAt(curPos))) {
                sb.append(source.charAt(curPos));
                curPos++;
            }
        }
        curType = LexType.INTCON;
        curToken = sb.toString();
    }

    private boolean isIdentifierFirstLetter(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private void parseIdentifierOrReservedWord() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(source.charAt(curPos)) || source.charAt(curPos) == '_') {
            sb.append(source.charAt(curPos));
            curPos++;
        }
        String identifier = sb.toString();
        if (reservedWords.containsKey(identifier)) {
            curType = LexType.valueOf(reservedWords.get(identifier));
        } else {
            curType = LexType.IDENFR;
        }
        curToken = identifier;
    }

    private boolean isLegalLetter(char c) {
        return (c >= 32 && c <= 126) || (c == 0);
    }

    private char parseEscape() {
        return source.charAt(curPos);
        /*
        return switch (source.charAt(curPos)) {
            case 'a' -> '\u0007';
            case 'b' -> '\b';
            case 't' -> '\t';
            case 'n' -> '\n';
            case 'v' -> '\u000B';
            case 'f' -> '\f';
            case '\"' -> '\"';
            case '\'' -> '\'';
            case '\\' -> '\\';
            case '0' -> '\0';
            default -> 127; // error
        };
        */
    }

    private void parseString() {
        StringBuilder sb = new StringBuilder();
        do {
            if (source.charAt(curPos) != '\\') {
                sb.append(source.charAt(curPos));
                curPos++;
            } else {
                sb.append(source.charAt(curPos));
                curPos++;
                char ch = parseEscape();
                if (ch == 127) {
                    curType = LexType.ERROR;
                    curToken = "";
                    return;
                } else {
                    sb.append(ch);
                    curPos++;
                }
            }
        } while (source.charAt(curPos) != '\"' && isLegalLetter(source.charAt(curPos)));
        sb.append(source.charAt(curPos));
        curPos++;
        curType = LexType.STRCON;
        curToken = sb.toString();
    }

    private void parseChar() {
        StringBuilder sb = new StringBuilder();
        sb.append(source.charAt(curPos));
        curPos++;
        if (isLegalLetter(source.charAt(curPos))) {
            if (source.charAt(curPos) != '\\') {
                sb.append(source.charAt(curPos));
                curPos++;
            } else {
                curPos++;
                char ch = parseEscape();
                if (ch == 127) {
                    curType = LexType.ERROR;
                    curToken = "";
                    return;
                } else {
                    sb.append(ch);
                    curPos++;
                }
            }
        } else {
            curType = LexType.ERROR;
            curToken = "";
            return;
        }
        if (source.charAt(curPos) == '\'') {
            sb.append(source.charAt(curPos));
            curPos++;
            curType = LexType.CHARCON;
            curToken = sb.toString();
        } else {
            curType = LexType.ERROR;
            curToken = "";
        }
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
            // TODO '/' 与注释
            case '/' -> parseNote();
            default -> {
                curToken = "";
                curType = LexType.ERROR;
                curPos++;
            }
        }
    }

    private void parseNote() {
        StringBuilder sb = new StringBuilder();
        sb.append(source.charAt(curPos++));
        if (curPos < source.length() && source.charAt(curPos) == '/') {
            sb.append(source.charAt(curPos++));
            while (curPos < source.length() && source.charAt(curPos) != '\n') {
                sb.append(source.charAt(curPos++));
            }
            if (curPos < source.length()) {
                sb.append(source.charAt(curPos++));
                lineNum++;
            }
            curToken = sb.toString();
            curType = LexType.NOTE;
        } else if (curPos < source.length() && source.charAt(curPos) == '*') {
            sb.append(source.charAt(curPos++));
            while (curPos < source.length()) {
                while (curPos < source.length() && source.charAt(curPos) != '*') {
                    if (source.charAt(curPos) == '\n') {
                        lineNum++;
                    }
                    sb.append(source.charAt(curPos++));
                }
                while (curPos < source.length() && source.charAt(curPos) == '*') {
                    sb.append(source.charAt(curPos++));
                }
                if (curPos < source.length() && source.charAt(curPos) == '/') {
                    sb.append(source.charAt(curPos++));
                    curToken = sb.toString();
                    curType = LexType.NOTE;
                    break;
                }
            }
        } else {
            curToken = "/";
            curType = LexType.ERROR;
        }
    }
}
