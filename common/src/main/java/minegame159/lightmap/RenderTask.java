package minegame159.lightmap;

import minegame159.lightmap.platform.LightChunk;
import minegame159.lightmap.task.Task;
import minegame159.lightmap.utils.LightChunkPos;

public class RenderTask extends Task {
    private final World world;
    private final LightChunk[] chunks = new LightChunk[9];

    private ChunkView view;

    public RenderTask(World world, LightChunk chunk) {
        this.world = world;

        setChunk(0, 0, chunk);
    }

    @Override
    public void runImpl() {
        LightChunkPos pos = getCurrent().getPos();

        Region region = world.getRegion(pos);
        view = region.getView(pos);

        ChunkRenderer.render(this);
        region.markChunk(pos);
    }

    public LightChunk getChunk(int offsetX, int offsetZ) {
        return chunks[(offsetZ + 1) * 3 + (offsetX + 1)];
    }

    public LightChunk getCurrent() {
        return chunks[4];
    }

    public void setChunk(int offsetX, int offsetZ, LightChunk chunk) {
        chunks[(offsetZ + 1) * 3 + (offsetX + 1)] = chunk;
    }

    public void set(int x, int z, int argb) {
        view.set(x, z, argb);
    }
}
