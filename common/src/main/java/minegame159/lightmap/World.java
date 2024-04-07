package minegame159.lightmap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import minegame159.lightmap.platform.LightChunk;
import minegame159.lightmap.platform.LightWorld;
import minegame159.lightmap.task.TaskQueue;
import minegame159.lightmap.utils.LightChunkPos;
import minegame159.lightmap.utils.LightId;

import java.nio.file.Path;
import java.util.Iterator;

public class World implements Iterable<Region> {
    private final LightId id;
    private final LightWorld platform;

    private final Path folder;

    private final Long2ObjectMap<Region> regions = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<PendingChunk> pendingChunks = new Long2ObjectOpenHashMap<>();

    public World(LightId id, Path folder) {
        this.id = id;
        this.platform = LightMap.get().getPlatform().getWorld(id);

        this.folder = folder;
    }

    public LightId getId() {
        return id;
    }

    public synchronized boolean isChunkRendered(LightChunkPos pos) {
        return getRegion(pos).containsChunk(pos);
    }

    public synchronized void markAsPending(LightChunkPos pos) {
        PendingChunk pendingChunk = pendingChunks.get(pos.toLong());

        if (pendingChunk == null) {
            pendingChunk = new PendingChunk();

            pendingChunks.put(pos.toLong(), pendingChunk);
            getRegion(pos).addPending();
        }

        pendingChunk.mark();
    }

    public synchronized void flushAndClose(int regionX, int regionZ) {
        long pos = Region.getRegionPos(regionX, regionZ);
        Region region = regions.get(pos);

        if (region != null) {
            region.write();
            regions.remove(pos);
        }
    }

    public synchronized Region getRegion(LightChunkPos pos) {
        Region region = regions.get(Region.getRegionPos(pos));

        if (region == null) {
            region = new Region(folder, Region.getRegionX(pos), Region.getRegionZ(pos));
            regions.put(Region.getRegionPos(pos), region);
        }

        return region;
    }

    public synchronized void enqueuePendingChunks(TaskQueue tasks) {
        long now = System.nanoTime();

        for (LongIterator it = pendingChunks.keySet().iterator(); it.hasNext(); ) {
            long pos = it.nextLong();
            PendingChunk pendingChunk = pendingChunks.get(pos);

            if (now - pendingChunk.modifiedAt > 500L * 1000000L) {
                it.remove();

                int x = LightChunkPos.getX(pos);
                int z = LightChunkPos.getZ(pos);

                LightChunk chunk = platform.getChunk(x, z);
                if (chunk == null) continue;

                RenderTask task = new RenderTask(this, chunk);

                for (int offsetX = -1; offsetX <= 1; offsetX++) {
                    for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                        LightChunk neighbour = platform.getChunk(x + offsetX, z + offsetZ);
                        task.setChunk(offsetX, offsetZ, neighbour);
                    }
                }

                tasks.add(task);
            }
        }
    }

    @Override
    public Iterator<Region> iterator() {
        return regions.values().iterator();
    }

    private static class PendingChunk {
        public long modifiedAt;

        public void mark() {
            this.modifiedAt = System.nanoTime();
        }
    }
}
