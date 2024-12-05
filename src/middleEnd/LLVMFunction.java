package middleEnd;

import frontEnd.Symbol;
import middleEnd.Insts.BranchInst;
import middleEnd.Insts.RetInst;

import java.util.LinkedList;

public class LLVMFunction extends Value {
    public LLVMType.TypeID retType;
    public final String name;
    public final LinkedList<FuncFParam> params = new LinkedList<>();
    private Block block;
    public LinkedList<LLVMBasicBlock> basicBlocks = new LinkedList<>();

    public LLVMFunction(Symbol symbol) {
        super();
        switch (symbol.symbolType) {
            case IntFunc -> this.retType = LLVMType.TypeID.IntegerTyID;
            case CharFunc -> this.retType = LLVMType.TypeID.CharTyID;
            case VoidFunc -> this.retType = LLVMType.TypeID.VoidTyID;
        }
        this.name = symbol.token.token;
        // System.out.println("Formal Parameters: " + symbol.params);
    }

    public LLVMFunction(String name, LLVMType.TypeID type) {
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

    public void generateBasicBlocks() {
        // 从 Block 中生成 BasicBlock
        int blockNo = 0;
        LLVMBasicBlock bb = new LLVMBasicBlock(String.format("%s_%d", name, blockNo++));
        LLVMLabel label = new LLVMLabel();
        label.setRegNo(0);
        for (LLVMInstruction inst : block.getInstructions()) {
            if (inst instanceof LLVMLabel) {
                label = (LLVMLabel) inst;
                continue;
            }
            bb.addInst(inst);
            if (inst instanceof BranchInst || inst instanceof RetInst) {
                bb.setLLVMLabel(label);
                basicBlocks.add(bb);
                bb = new LLVMBasicBlock(String.format("%s_%d", name, blockNo++));
            }
        }
    }
}
