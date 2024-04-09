package minegame159.lightmap;

import minegame159.lightmap.events.ChunkLoadEvent;
import minegame159.lightmap.events.TickEvent;
import minegame159.lightmap.platform.LightPlatform;
import minegame159.lightmap.platform.LightWorld;
import minegame159.lightmap.resources.ResourceManager;
import minegame159.lightmap.server.LightServer;
import minegame159.lightmap.task.TaskQueue;
import minegame159.lightmap.utils.LightChunkPos;
import minegame159.lightmap.utils.LightId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class LightMap {
    public static final Logger LOG = LoggerFactory.getLogger("LightMap");
    private static LightMap INSTANCE;

    private final LightPlatform platform;
    private final ResourceManager resources;

    private final TaskQueue tasks;
    private final Map<LightId, World> worlds;

    private WorldImporter importer;
    private TaskQueue importQueue;
    private ImportRegionTask importTask;
    private int importTimer;

    private final LightServer server;

    private LightMap(LightPlatform platform) {
        this.platform = platform;
        this.resources = new ResourceManager(platform);

        this.tasks = new TaskQueue(null);
        this.worlds = new HashMap<>();

        this.server = new LightServer();
    }

    private void init() {
        try {
            Files.createDirectories(platform.getDataFolder());

            try (Stream<Path> list = Files.list(platform.getDataFolder())) {
                list.forEach(path -> {
                    LightId id = LightId.of(path.getFileName().toString());
                    getOrCreateWorld(id);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ChunkLoadEvent.register(this::onChunkLoad);
        TickEvent.register(this::onTick);
    }

    public LightPlatform getPlatform() {
        return platform;
    }

    public ResourceManager getResources() {
        return resources;
    }

    public void importWorld(LightId id) {
        if (importer != null) return;

        World world = getOrCreateWorld(id);
        Path folder = platform.getWorld(id).getFolder().resolve("region");

        importer = new WorldImporter(world, folder);
        importQueue = new TaskQueue("Region Importer");
        importTimer = 0;

        if (importer.hasMore()) {
            importTask = importer.getImportRegionTask();
            importQueue.add(importTask);
        }

        LOG.info("Starting world import for {}", id);
    }

    private void onChunkLoad(LightWorld lightWorld, int x, int z) {
        World world = getOrCreateWorld(lightWorld.getId());
        LightChunkPos pos = new LightChunkPos(x, z);

        if (!world.isChunkRendered(pos)) {
            world.markAsPending(pos);
        }
    }

    private World getOrCreateWorld(LightId id) {
        World world = worlds.get(id);

        if (world == null) {
            Path folder = platform.getDataFolder().resolve(id.toString());

            world = new World(id, folder);
            worlds.put(id, world);
        }

        return world;
    }

    private void onTick() {
        // Worlds
        for (World world : worlds.values()) {
            // Pending chunks
            world.enqueuePendingChunks(tasks);

            // Region writes
            for (Region region : world) {
                region.scheduleWrite(tasks);
            }
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

    // Static

    public static LightMap get() {
        return INSTANCE;
    }

    public static void init(LightPlatform platform) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Lightmap is already initialized");
        }

        LOG.info("Starting Light Map");

        INSTANCE = new LightMap(platform);
        INSTANCE.init();
    }
}
