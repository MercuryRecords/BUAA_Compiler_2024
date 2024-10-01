package frontend;

import frontend.lexer.LexType;

public class Token {
    public LexType type;
    public String token;
    public int lineNum;
    public Token(LexType curType, String curToken, int lineNum) {
        this.type = curType;
        this.token = curToken;
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return type + " " + token;
    }
}
