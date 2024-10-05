package frontend;

public class LeafASTNode extends ASTNode{
    private final Token token;
    public LeafASTNode(Token token) {
        super("LEAF");
        this.token = token;
        this.children = null;
    }
}