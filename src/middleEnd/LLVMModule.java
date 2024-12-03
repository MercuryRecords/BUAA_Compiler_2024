package middleEnd;

import backEnd.MIPSDataSection;

import java.util.LinkedList;

public class LLVMModule {
    // <CompUnit> ::= {<Decl>} {<FuncDef>} <MainFuncDef>
    private final LinkedList<Value> globalValues = new LinkedList<>();
    private final LinkedList<Value> functions = new LinkedList<>();

    public void addGlobalDecls(LinkedList<Value> values) {
        globalValues.addAll(values);
    }

    public void addStrDecl(Value value) {
        globalValues.add(value);
    }


    public void addFunction(Function globalValue) {
        functions.add(globalValue);
    }

    @Override
    public String toString() {
        StringBuilder module = new StringBuilder();
        for (Value value : globalValues) {
            module.append(value.toString()).append("\n");
        }
        for (Value value : functions) {
            module.append(value.toString()).append("\n");
        }
        return module.toString();
    }

    public MIPSDataSection translateToMIPSDataSection() {
        MIPSDataSection newSection = new MIPSDataSection();
        for (Value value : globalValues) {
            if (value instanceof GlobalVariable) {
                newSection.addGlobalVariable((GlobalVariable) value);
            } else {
                newSection.addString((GlobalString) value);
            }
        }
        return newSection;
    }
}
