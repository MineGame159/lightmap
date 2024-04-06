package minegame159.lightmap.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskQueue {
    private final ArrayBlockingQueue<Task> tasks = new ArrayBlockingQueue<>(32 * 32 + 8);
    private final AtomicInteger taskCount = new AtomicInteger();

    private final Thread thread;
    private volatile boolean running;

    public TaskQueue(String name) {
        String fullName = "LightMap - Worker";

        if (name != null) {
            fullName += " - " + name;
        }

        thread = new Thread(this::run, fullName);
        thread.start();

        running = true;
    }

    public int count() {
        return taskCount.get();
    }

    public void add(Task task) {
        if (running && !tasks.offer(task)) {
            throw new IllegalStateException("LightMap - TaskQueue is full, cannot add a new task");
        }
        else {
            taskCount.getAndIncrement();
        }
    }

    public void stop() {
        add(StopTask.INSTANCE);
        running = false;

        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void run() {
        while (true) {
            try {
                Task task = tasks.take();
                if (!running) return;

                task.run();
                taskCount.getAndDecrement();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class StopTask extends Task {
        private static final StopTask INSTANCE = new StopTask();

        @Override
        protected void runImpl() {
            throw new IllegalStateException("LightMap - TaskQueue - Stop task ran, bad");
        }
    }
}
