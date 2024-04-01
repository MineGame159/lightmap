package minegame159.lightmap.task;

public abstract class Task {
    private volatile boolean finished;

    public boolean isFinished() {
        return finished;
    }

    public void run() {
        runImpl();
        finished = true;
    }

    protected abstract void runImpl();
}
