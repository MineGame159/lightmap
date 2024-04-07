package minegame159.lightmap.platform;

import minegame159.lightmap.utils.LightChunkPos;

public interface LightChunk {
    LightWorld getWorld();

    LightChunkPos getPos();

    int getTopY(int x, int z);
    int getBottomY();

    LightBlock getBlock(int x, int y, int z);
    LightBiome getBiome(int x, int y, int z);
}
