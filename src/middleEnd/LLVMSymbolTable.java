package middleEnd;

import java.util.HashMap;

public class LLVMSymbolTable {
    public int id;
    public LLVMSymbolTable parentTable;
    public HashMap<String, LLVMSymbol> symbols = new HashMap<>();

    public LLVMSymbolTable(int scopeId, LLVMSymbolTable parentTable) {
        this.id = scopeId;
        this.parentTable = parentTable;
    }


    public void addSymbol(LLVMSymbol symbol) {
        symbols.put(symbol.name, symbol);
    }
}
