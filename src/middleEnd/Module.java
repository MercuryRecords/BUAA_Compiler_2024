package middleEnd;

import java.util.LinkedList;

public class Module {
    // <CompUnit> ::= {<Decl>} {<FuncDef>} <MainFuncDef>
    private final LinkedList<GlobalValue> globalValues = new LinkedList<>();


    public void addGlobalValue(GlobalValue globalValue) {
        globalValues.add(globalValue);
    }

    public void addGlobalValues(LinkedList<GlobalValue> values) {
        globalValues.addAll(values);
    }

    @Override
    public String toString() {
        StringBuilder module = new StringBuilder();
        for (GlobalValue globalValue : globalValues) {
            module.append(globalValue.toString());
        }
        return module.toString();
    }
}
