package minegame159.lightmap.platform;

import minegame159.lightmap.mixininterface.IBiome;
import minegame159.lightmap.mixininterface.IBlockState;
import minegame159.lightmap.utils.LightChunkPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.WorldChunk;

public class FabricChunk implements LightChunk {
    private final BlockPos.Mutable POS = new BlockPos.Mutable();

    private final LightChunkPos pos;
    private final WorldChunk chunk;
    private final Heightmap heightmap;

    public FabricChunk(WorldChunk chunk) {
        this.pos = new LightChunkPos(chunk.getPos().x, chunk.getPos().z);
        this.chunk = chunk;
        this.heightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE);
    }

    @Override
    public LightChunkPos getPos() {
        return pos;
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
    public LightBlock getBlock(int x, int y, int z) {
        POS.set(x, y, z);
        return ((IBlockState) chunk.getBlockState(POS)).lightmap$getBlock();
    }

    @Override
    public LightBiome getBiome(int x, int y, int z) {
        Biome biome = chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(x), BiomeCoords.fromBlock(y), BiomeCoords.fromBlock(z)).value();
        return ((IBiome) (Object) biome).lightmap$getBiome();
    }
}
