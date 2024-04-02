package minegame159.lightmap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import minegame159.lightmap.commands.Commands;
import minegame159.lightmap.importer.ImportRegionTask;
import minegame159.lightmap.importer.WorldImporter;
import minegame159.lightmap.mixin.MinecraftServerAccessor;
import minegame159.lightmap.models.BlockColors;
import minegame159.lightmap.server.LightServer;
import minegame159.lightmap.task.TaskQueue;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LightMap implements DedicatedServerModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("LightMap");

    private static LightMap INSTANCE;

    private File dataDirectory;

    private TaskQueue tasks;
    private LightWorld world;

    private final Long2ObjectMap<PendingChunk> pendingChunks = new Long2ObjectOpenHashMap<>();

    private WorldImporter importer;
    private TaskQueue importQueue;
    private ImportRegionTask importTask;
    private int importTimer;

    private LightServer server;

    @Override
    public void onInitializeServer() {
        INSTANCE = this;

        Commands.register();

        ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
    }

    public static LightMap getInstance() {
        return INSTANCE;
    }

    public LightWorld getWorld() {
        return world;
    }

    public void importWorld() {
        if (importer != null) return;

        ServerWorld world = this.world.world.getServer().getWorld(World.OVERWORLD);
        File directory = new File(world.getChunkManager().threadedAnvilChunkStorage.getSaveDir(), "region");

        importer = new WorldImporter(world, directory);
        importQueue = new TaskQueue("Region Importer");
        importTimer = 0;

        if (importer.hasMore()) {
            importTask = importer.getImportRegionTask();
            importQueue.add(importTask);
        }

        LOG.info("Starting world import");
    }

    private static int toAbgr(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb) & 0xFF;
        int a = (argb >> 24) & 0xFF;

        return (a << 24) | (b << 16) | (g << 8) | (r);
    }

    private static int[] loadColorMap(String path) {
        try {
            BufferedImage image = ImageIO.read(LightMap.class.getResource(path));
            int[] colors = new int[256 * 256];

            for (int x = 0; x < 256; x++) {
                for (int y = 0; y < 256; y++) {
                    colors[y * 256 + x] = toAbgr(image.getRGB(x, y));
                }
            }

            return colors;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        if (world.getRegistryKey() != World.OVERWORLD || importer != null) return;

        if (this.world == null) {
            LOG.info("Starting LightMap");

            GrassColors.setColorMap(loadColorMap("/grass.png"));
            FoliageColors.setColorMap(loadColorMap("/foliage.png"));
            LightBiome.init(world.getServer());

            dataDirectory = new File(new File(((MinecraftServerAccessor) world.getServer()).light$session().getWorldDirectory(World.OVERWORLD).toFile(), "data"), "lightmap");
            dataDirectory.mkdirs();

            tasks = new TaskQueue(null);
            this.world = new LightWorld(world, dataDirectory);

            this.server = new LightServer(dataDirectory);

            ServerTickEvents.END_SERVER_TICK.register(this::onTick);
            ServerLifecycleEvents.SERVER_STOPPING.register(this::onStop);
        }

        if (!this.world.isChunkRendered(chunk)) {
            markAsPending(chunk);
        }
    }

    private synchronized void onTick(MinecraftServer server) {
        // Pending chunks
        long now = System.nanoTime();

        for (LongIterator it = pendingChunks.keySet().iterator(); it.hasNext();) {
            long pos = it.nextLong();
            PendingChunk pendingChunk = pendingChunks.get(pos);

            if (now - pendingChunk.modifiedAt > 500L * 1000000L) {
                tasks.add(new RenderTask(
                        world,
                        new ChunkWrapper(pendingChunk.chunk)
                ));

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

    private void markAsPending(WorldChunk chunk) {
        long pos = chunk.getPos().toLong();
        PendingChunk pendingChunk = pendingChunks.get(pos);

        if (pendingChunk == null) {
            pendingChunk = new PendingChunk(chunk);

            pendingChunks.put(pos, pendingChunk);
            world.getRegion(chunk.getPos()).addPending();
        }

        pendingChunk.modifiedAt = System.nanoTime();
    }

    private void onStop(MinecraftServer server) {
        LOG.info("Stopping LightMap");

        this.server.stop();
        tasks.stop();
        BlockColors.close();
    }

    private static class PendingChunk {
        private final WorldChunk chunk;

        public long modifiedAt;

        private PendingChunk(WorldChunk chunk) {
            this.chunk = chunk;
        }
    }
}
