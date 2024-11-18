package middleEnd;

import middleEnd.utils.RegTracker;

public class InitVal extends Value {
    private RegTracker tracker;
    // 供 ConstInitVal 使用
    public InitVal() {
        super();
    }

    public InitVal(RegTracker tracker) {
        super();
        this.tracker = tracker;
    }

    public void addExp() {

    }
}
