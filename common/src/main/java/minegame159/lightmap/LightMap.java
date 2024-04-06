package minegame159.lightmap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import minegame159.lightmap.platform.LightChunk;
import minegame159.lightmap.platform.LightPlatform;
import minegame159.lightmap.resources.ResourceManager;
import minegame159.lightmap.server.LightServer;
import minegame159.lightmap.task.TaskQueue;
import minegame159.lightmap.utils.LightId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class LightMap {
    public static final Logger LOG = LoggerFactory.getLogger("LightMap");
    private static LightMap INSTANCE;

    private final LightPlatform platform;
    private final ResourceManager resources;

    private final TaskQueue tasks;
    private final LightWorld world;

    private final Long2ObjectMap<PendingChunk> pendingChunks = new Long2ObjectOpenHashMap<>();

    private WorldImporter importer;
    private TaskQueue importQueue;
    private ImportRegionTask importTask;
    private int importTimer;

    private final LightServer server;

    private LightMap(LightPlatform platform, LightId worldId) {
        File dataFolder = platform.getDataFolder().toFile();
        dataFolder.mkdirs();

        this.platform = platform;
        this.resources = new ResourceManager(platform);

        this.tasks = new TaskQueue(null);
        this.world = new LightWorld(worldId, dataFolder);

        this.server = new LightServer(dataFolder);
    }

    public LightPlatform getPlatform() {
        return platform;
    }

    public ResourceManager getResources() {
        return resources;
    }

    public LightWorld getWorld() {
        return world;
    }

    public void importWorld() {
        if (importer != null) return;

        Path folder = platform.getWorldFolder(world.getId()).resolve("region");

        importer = new WorldImporter(folder);
        importQueue = new TaskQueue("Region Importer");
        importTimer = 0;

        if (importer.hasMore()) {
            importTask = importer.getImportRegionTask();
            importQueue.add(importTask);
        }

        LOG.info("Starting world import");
    }

    public synchronized void markAsPending(LightChunk chunk) {
        if (importer != null) return;

        long pos = chunk.getPos().toLong();
        PendingChunk pendingChunk = pendingChunks.get(pos);

        if (pendingChunk == null) {
            pendingChunk = new PendingChunk(chunk);

            pendingChunks.put(pos, pendingChunk);
            world.getRegion(chunk.getPos()).addPending();
        }

        pendingChunk.mark(chunk);
    }

    public synchronized void tick() {
        // Pending chunks
        long now = System.nanoTime();

        for (LongIterator it = pendingChunks.keySet().iterator(); it.hasNext();) {
            long pos = it.nextLong();
            PendingChunk pendingChunk = pendingChunks.get(pos);

            if (now - pendingChunk.modifiedAt > 500L * 1000000L) {
                tasks.add(new RenderTask(world, pendingChunk.chunk));
                it.remove();
            }
        }

        // Region writes
        for (LightRegion region : world) {
            region.scheduleWrite(tasks);
        }

        // Importer
        if (importer != null) {
            tickImporter();
        }
    }

    private void tickImporter() {
        // Log progress
        if (importTimer % 20 == 0) {
            LOG.info("World import progress: {} / {} regions", importer.finishedCount(), importer.count());
        }

        importTimer++;

        // Main task queue empty
        if (tasks.count() == 0) {
            if (importTask != null && importTask.isFinished()) {
                importTask.enqueue(tasks);
                importTask = null;

                if (importer.hasMore()) {
                    importTask = importer.getImportRegionTask();
                    importQueue.add(importTask);
                }
            }

            if (importTask == null) {
                LOG.info("World import finished, took {} ticks, {} seconds", importTimer, importTimer / 20.0);

                importQueue.stop();

                importTask = null;
                importQueue = null;
                importer = null;
            }
        }
    }

    public void stop() {
        LOG.info("Stopping Light Map");

        server.stop();
        tasks.stop();
        resources.close();
    }

    private static class PendingChunk {
        private LightChunk chunk;
        public long modifiedAt;

        private PendingChunk(LightChunk chunk) {
            this.chunk = chunk;
        }

        public void mark(LightChunk chunk) {
            this.chunk = chunk;
            this.modifiedAt = System.nanoTime();
        }
    }

    // Static

    public static LightMap get() {
        return INSTANCE;
    }

    public static void init(LightPlatform platform, LightId worldId) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Lightmap is already initialized");
        }

        LOG.info("Starting Light Map");
        INSTANCE = new LightMap(platform, worldId);
    }
}
