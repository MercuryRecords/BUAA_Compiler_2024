package middleEnd;

import java.util.ArrayList;

public class InitVal extends Value {
    private final ArrayList<Integer> constExpList;
//    private final ArrayList<Value> expList;
    public InitVal(String StringConst) {
        super();
        constExpList = new ArrayList<>();
//        expList = null;
        for (char c : StringConst.toCharArray()) {
            constExpList.add((int) c);
        }
    }

    public InitVal() {
        super();
        constExpList = new ArrayList<>();
//        expList = new ArrayList<>();
    }

    public void addConstExp(int i) {
        constExpList.add(i);
    }

//    public void addExp(Value value) {
//        expList.add(value);
//    }

    public int getConstValue(int i) {
        return constExpList.get(i);
    }

    public int getConstLength() {
        return constExpList.size();
    }
}
