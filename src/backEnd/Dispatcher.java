package backEnd;

public class Dispatcher {
    private static final Dispatcher dispatcher = new Dispatcher();
    private int offset = 0;
    private Dispatcher() {
    }

    public static Dispatcher getInstance() {
        return dispatcher;
    }

    public int getOffset() {
        return offset;
    }

    public void addOffset(int size) {
        offset += size;
    }

}
