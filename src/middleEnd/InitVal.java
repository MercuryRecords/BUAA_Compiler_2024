package middleEnd;

import java.util.ArrayList;

public class InitVal extends Value {
    private final ArrayList<Integer> constExpList;
    public InitVal(String StringConst) {
        super();
        constExpList = new ArrayList<>();
        // 记得转义字符
        boolean isBackslash = false;
        for (char c : StringConst.toCharArray()) {
            if (c == '\\' && !isBackslash) {
                isBackslash = true;
                continue;
            }
            if (isBackslash) {
                isBackslash = false;
                switch (c) {
                    case 'a' -> addConstExp(7);
                    case 'b' -> addConstExp(8);
                    case 't' -> addConstExp(9);
                    case 'n' -> addConstExp(10);
                    case 'v' -> addConstExp(11);
                    case 'f' -> addConstExp(12);
                    case '\"' -> addConstExp(34);
                    case '\'' -> addConstExp(39);
                    case '\\' -> addConstExp(92);
                    case '0' -> addConstExp(0);
                    default -> throw new RuntimeException("Invalid escape character: \\" + c);
                }
            } else {
                addConstExp(c);
            }
        }
    }

    public InitVal() {
        super();
        constExpList = new ArrayList<>();
    }

    public void addConstExp(int i) {
        constExpList.add(i);
    }

    public int getConstValue(int i) {
        return constExpList.get(i);
    }

    public int getConstLength() {
        return constExpList.size();
    }
}
