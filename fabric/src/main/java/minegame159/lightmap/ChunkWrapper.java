package minegame159.lightmap;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkWrapper implements LightChunk {
    private final BlockPos.Mutable pos = new BlockPos.Mutable();

    private final WorldChunk chunk;
    private final Heightmap heightmap;

    public ChunkWrapper(WorldChunk chunk) {
        this.chunk = chunk;
        this.heightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE);
    }

    @Override
    public ChunkPos getPos() {
        return chunk.getPos();
    }

    @Override
    public int getTopY(int x, int z) {
        return heightmap.get(x, z);
    }

    @Override
    public int getBottomY() {
        return chunk.getBottomY();
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        pos.set(x, y, z);
        return chunk.getBlockState(pos);
    }

    @Override
    public LightBiome getBiome(int x, int y, int z) {
        Biome biome = chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(x), BiomeCoords.fromBlock(y), BiomeCoords.fromBlock(z)).value();
        return LightBiome.get(biome);
    }
}
