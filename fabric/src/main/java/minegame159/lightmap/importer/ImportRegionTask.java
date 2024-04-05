package minegame159.lightmap.importer;

import minegame159.lightmap.task.Task;
import minegame159.lightmap.task.TaskQueue;

import java.util.ArrayList;
import java.util.List;

public class ImportRegionTask extends Task {
    private final WorldImporter importer;
    private final List<Task> tasks = new ArrayList<>();

    ImportRegionTask(WorldImporter importer) {
        this.importer = importer;
    }

    @Override
    protected void runImpl() {
        importer.importRegion(tasks);
    }

    public void enqueue(TaskQueue tasks) {
        for (Task task : this.tasks) {
            tasks.add(task);
        }
    }
}
