package middleEnd;

import java.util.LinkedList;

public class Module {
    // <CompUnit> ::= {<Decl>} {<FuncDef>} <MainFuncDef>
    private final LinkedList<Value> globalValues = new LinkedList<>();


    public void addValue(Value globalValue) {
        globalValues.add(globalValue);
    }

    public void addValues(LinkedList<Value> values) {
        globalValues.addAll(values);
    }

    @Override
    public String toString() {
        StringBuilder module = new StringBuilder();
        for (Value value : globalValues) {
            module.append(value.toString()).append("\n");
        }
        return module.toString();
    }
}
