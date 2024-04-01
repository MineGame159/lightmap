package minegame159.lightmap;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskQueue {
    private final ArrayBlockingQueue<Runnable> tasks = new ArrayBlockingQueue<>(32 * 32 + 8);
    private final AtomicInteger taskCount = new AtomicInteger();

    private final Thread thread = new Thread(this::run, "LightMap - Worker");
    private volatile boolean running;

    public TaskQueue() {
        thread.start();
        running = true;
    }

    public int count() {
        return taskCount.get();
    }

    public void add(Runnable task) {
        if (running && !tasks.offer(task)) {
            throw new IllegalStateException("LightMap - TaskQueue is full, cannot add a new task");
        }
        else {
            taskCount.getAndIncrement();
        }
    }

    public void stop() {
        add(TaskQueue::stopTask);
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
                Runnable task = tasks.take();
                if (!running) return;

                task.run();
                taskCount.getAndDecrement();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void stopTask() {
        throw new IllegalStateException("LightMap - TaskQueue - Stop task ran, bad");
    }
}
