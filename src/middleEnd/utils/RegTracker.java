package middleEnd.utils;

public class RegTracker {
    private final int scopeId;
    private int regNo = 0; // 已经用过的编号

    public RegTracker(int scopeId) {
        this.scopeId = scopeId;
    }

    public RegTracker() {
        scopeId = -1;
        regNo = 1;
    }

    public int nextRegNo() {
        return regNo++;
    }
}
