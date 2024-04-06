package minegame159.lightmap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import minegame159.lightmap.utils.LightChunkPos;
import minegame159.lightmap.utils.LightId;

import java.io.File;
import java.util.Iterator;

public class LightWorld implements Iterable<LightRegion> {
    private final LightId id;
    private final File directory;

    private final Long2ObjectMap<LightRegion> regions = new Long2ObjectOpenHashMap<>();

    public LightWorld(LightId id, File directory) {
        this.id = id;
        this.directory = directory;
    }

    public LightId getId() {
        return id;
    }

    public synchronized boolean isChunkRendered(LightChunkPos pos) {
        return getRegion(pos).containsChunk(pos);
    }

    public synchronized void flushAndClose(int regionX, int regionZ) {
        long pos = LightRegion.getRegionPos(regionX, regionZ);
        LightRegion region = regions.get(pos);

        if (region != null) {
            region.write();
            regions.remove(pos);
        }
    }

    public synchronized LightRegion getRegion(LightChunkPos pos) {
        LightRegion region = regions.get(LightRegion.getRegionPos(pos));

        if (region == null) {
            region = new LightRegion(directory, LightRegion.getRegionX(pos), LightRegion.getRegionZ(pos));
            regions.put(LightRegion.getRegionPos(pos), region);
        }

        return region;
    }

    @Override
    public Iterator<LightRegion> iterator() {
        return regions.values().iterator();
    }
}
