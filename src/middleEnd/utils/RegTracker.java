package middleEnd.utils;

public class RegTracker {
    private final int scopeId;
    private int regNo = 0; // 已经用过的编号

    public RegTracker(int scopeId) {
        this.scopeId = scopeId;
    }

    public int nextRegNo() {
        return ++regNo;
    }
}
