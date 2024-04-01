package minegame159.lightmap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Iterator;

public class LightWorld implements Iterable<LightRegion> {
    public final ServerWorld world;
    private final File directory;

    private final Long2ObjectMap<LightRegion> regions = new Long2ObjectOpenHashMap<>();

    public LightWorld(ServerWorld world, File directory) {
        this.world = world;
        this.directory = directory;
    }

    public synchronized boolean isChunkRendered(WorldChunk chunk) {
        return getRegion(chunk.getPos()).containsChunk(chunk.getPos());
    }

    public synchronized void flushAndClose(int regionX, int regionZ) {
        long pos = LightRegion.getRegionPos(regionX, regionZ);
        LightRegion region = regions.get(pos);

        if (region != null) {
            region.write();
            regions.remove(pos);
        }
    }

    public synchronized LightRegion getRegion(ChunkPos pos) {
        LightRegion region = regions.get(LightRegion.getRegionPos(pos));

        if (region == null) {
            region = new LightRegion(directory, LightRegion.getRegionX(pos), LightRegion.getRegionZ(pos));
            regions.put(LightRegion.getRegionPos(pos), region);
        }

        return region;
    }

    @NotNull
    @Override
    public Iterator<LightRegion> iterator() {
        return regions.values().iterator();
    }
}
