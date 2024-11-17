package middleEnd;

import java.util.HashMap;

public class LLVMSymbolTable {
    public int id;
    public LLVMSymbolTable parentTable;
    public HashMap<String, LLVMVariable> symbols = new HashMap<>();

    public LLVMSymbolTable(int scopeId, LLVMSymbolTable parentTable) {
        this.id = scopeId;
        this.parentTable = parentTable;
    }


    public void addVariable(LLVMVariable var) {
        symbols.put(var.name, var);
    }
}
