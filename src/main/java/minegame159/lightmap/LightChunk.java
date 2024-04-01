package minegame159.lightmap;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

public interface LightChunk {
    ChunkPos getPos();

    int getTopY(int x, int z);
    int getBottomY();

    BlockState getBlockState(int x, int y, int z);
    Biome getBiome(int x, int y, int z);
}
