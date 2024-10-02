package frontend;

import frontend.lexer.LexType;

public class Token {
    public final LexType type;
    public final String token;
    public final int lineNum;
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
