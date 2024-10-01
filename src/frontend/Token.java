package frontend;

import frontend.Lexer.LexType;

public class Token {
    public LexType type;
    public String token;
    public int lineNum;
    public Token(LexType curType, String curToken, int lineNum) {
        this.type = curType;
        this.token = curToken;
        this.lineNum = lineNum;
    }
}
