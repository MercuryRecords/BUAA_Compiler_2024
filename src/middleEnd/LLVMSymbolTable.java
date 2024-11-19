package middleEnd;

import java.util.HashMap;

public class LLVMSymbolTable {
    public int id;
    public LLVMSymbolTable parentTable;
    public HashMap<String, UsableValue> symbols = new HashMap<>();

    public LLVMSymbolTable(int scopeId, LLVMSymbolTable parentTable) {
        this.id = scopeId;
        this.parentTable = parentTable;
    }


    public void addVariable(LLVMVariable var) {
        symbols.put(var.name, var);
    }

    public UsableValue get(String token) {
        return symbols.get(token);
    }

    public boolean hasVariable(String token) {
        return symbols.containsKey(token);
    }
}
