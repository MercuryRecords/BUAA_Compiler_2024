package middleEnd;

import frontEnd.SymbolTable;

import java.util.HashMap;

public class LLVMSymbolTable {
    public int id;
    public LLVMSymbolTable parentTable;
    public HashMap<String, LLVMSymbol> symbols = new HashMap<>();


    public void addSymbol(LLVMSymbol symbol) {
        symbols.put(symbol.name, symbol);
    }
}
