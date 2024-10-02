package frontend;

import java.util.ArrayList;

public class ASTNode {
    public String name;
    public String value;
    public ASTNode parent;
    public ArrayList<ASTNode> children;
    public ASTNode(String name, String value, ASTNode parent, ArrayList<ASTNode> children) {
        this.name = name;
        this.value = value;
        this.parent = parent;
        this.children = children;
    }
}
