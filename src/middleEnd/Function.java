package middleEnd;

import frontEnd.Symbol;

import java.util.LinkedList;

public class Function extends Value {
    private LLVMType.TypeID retType;
    private final String name;
    public final LinkedList<FuncFParam> params = new LinkedList<>();
    private Block block;

    public Function(Symbol symbol) {
        super();
        switch (symbol.symbolType) {
            case IntFunc -> this.retType = LLVMType.TypeID.IntegerTyID;
            case CharFunc -> this.retType = LLVMType.TypeID.CharTyID;
            case VoidFunc -> this.retType = LLVMType.TypeID.VoidTyID;
        }
        this.name = symbol.token.token;
        // System.out.println("Formal Parameters: " + symbol.params);
    }

    public Function(String name, LLVMType.TypeID type) {
        super();
        this.retType = type;
        this.name = name;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ");
        sb.append(retType.toString());
        sb.append(" @");
        sb.append(name);
        sb.append("(");
        if (!params.isEmpty()) {
            for (FuncFParam param : params) {
                sb.append(param.toString());
                sb.append(", ");
            }
            // remove the last comma and space
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
        sb.append(block.toString());
        sb.append("\n");
        return sb.toString();
    }

    public void setFParams(LinkedList<FuncFParam> params) {
        this.params.addAll(params);
    }

    public LLVMType.TypeID getReturnType() {
        return retType;
    }
}
