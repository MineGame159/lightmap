package minegame159.lightmap;

import net.minecraft.util.math.ChunkPos;

public class RenderTask implements Runnable {
    private final LightWorld world;
    private final LightChunk chunk;

    public RenderTask(LightWorld world, LightChunk chunk) {
        this.world = world;
        this.chunk = chunk;
    }

    @Override
    public void run() {
        ChunkPos pos = chunk.getPos();

        LightRegion region = world.getRegion(pos);
        LightChunkView view = region.getView(pos);

        ChunkRenderer.render(chunk, view);
        region.markChunk(pos);
    }
}
