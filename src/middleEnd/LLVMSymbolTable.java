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
        if (var instanceof GlobalVariable globalVariable) {
            symbols.put(globalVariable.name, globalVariable);
        } else {
            symbols.put(var.name, var.usableValue);
        }
    }

    public UsableValue get(String token) {
        return symbols.get(token);
    }

    public boolean hasVariable(String token) {
        return symbols.containsKey(token);
    }
}
