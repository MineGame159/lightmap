package minegame159.lightmap;

import minegame159.lightmap.platform.LightChunk;
import minegame159.lightmap.platform.LightRegionImporter;
import minegame159.lightmap.task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WorldImporter {
    private final World world;
    private final Path[] paths;

    private int i;

    public WorldImporter(World world, Path folder) {
        this.world = world;

        try {
            paths = Files.list(folder).toArray(Path[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int finishedCount() {
        return i;
    }

    public int count() {
        return paths.length;
    }

    public boolean hasMore() {
        return i < paths.length;
    }

    public ImportRegionTask getImportRegionTask() {
        return new ImportRegionTask(this);
    }

    void importRegion(List<Task> tasks) {
        LightRegionImporter region = getImporter();
        if (region == null) return;

        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                LightChunk chunk = region.getChunk(x, z);
                if (chunk == null) continue;

                RenderTask task = new RenderTask(world, chunk);

                for (int offsetX = -1; offsetX <= 1; offsetX++) {
                    for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                        int neighbourX = x + offsetX;
                        int neighbourZ = z + offsetZ;

                        if (neighbourX < 0 || neighbourX >= 32 || neighbourZ < 0 || neighbourZ >= 32) {
                            continue;
                        }

                        LightChunk neighbour = region.getChunk(neighbourX, neighbourZ);
                        task.setChunk(offsetX, offsetZ, neighbour);
                    }
                }

                tasks.add(task);
            }
        }

        tasks.add(new FlushRegionTask(region.getX(), region.getZ()));
    }

    private LightRegionImporter getImporter() {
        while (i < paths.length) {
            Path path = paths[i++];
            if (!path.getFileName().toString().endsWith(".mca")) continue;

            LightRegionImporter region = LightMap.get().getPlatform().createRegionImporter(path);
            if (region != null) return region;
        }

        return null;
    }

    private class FlushRegionTask extends Task {
        private final int x, z;

        private FlushRegionTask(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        protected void runImpl() {
            world.flushAndClose(x, z);
        }
    }
}
