package minegame159.lightmap.importer;

import com.mojang.serialization.Codec;
import minegame159.lightmap.LightChunk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;

import java.util.function.Predicate;

public class NbtChunk implements LightChunk {
    private final World world;
    private final ChunkPos pos;
    private final Codec<ReadableContainer<RegistryEntry<Biome>>> biomeCodec;

    private final Section[] sections;
    private final PackedIntegerArray heightmap;

    public NbtChunk(World world, ChunkPos pos, Codec<ReadableContainer<RegistryEntry<Biome>>> biomeCodec, NbtCompound nbt) {
        this.world = world;
        this.pos = pos;
        this.biomeCodec = biomeCodec;

        // Sections

        NbtList sectionsNbt = nbt.getList("sections", NbtElement.COMPOUND_TYPE);
        sections = new Section[sectionsNbt.size()];

        for (int i = 0; i < sectionsNbt.size(); i++) {
            sections[i] = new Section(sectionsNbt.getCompound(i));
        }

        // Heightmap

        long[] heightmapData = nbt.getCompound("Heightmaps").getLongArray("WORLD_SURFACE");
        if (heightmapData.length == 0) heightmapData = null;

        int elementBits = MathHelper.ceilLog2(world.getHeight() + 1);
        heightmap = new PackedIntegerArray(elementBits, 256, heightmapData);

        if (heightmapData == null) calculateHeightmap();
    }

    @Override
    public ChunkPos getPos() {
        return pos;
    }

    @Override
    public int getTopY(int x, int z) {
        return world.getBottomY() + heightmap.get(x + z * 16);
    }

    @Override
    public int getBottomY() {
        return world.getBottomY();
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        Section section = getSection(y);
        return section.getBlockState(x, y & 15, z);
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        Section section = getSection(y);
        return section.getBiome(BiomeCoords.fromBlock(x), BiomeCoords.fromBlock(y) & 3, BiomeCoords.fromBlock(z));
    }

    private Section getSection(int y) {
        return sections[(y - world.getBottomY()) >>> 4];
    }

    private void calculateHeightmap() {
        int maxY = 0;

        for (int i = sections.length - 1; i >= 0; i--) {
            if (!sections[i].isEmpty()) {
                maxY = (i << 4) + world.getBottomY() + 15;
                break;
            }
        }

        int minY = world.getBottomY();
        Predicate<BlockState> predicate =  Heightmap.Type.WORLD_SURFACE.getBlockPredicate();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = maxY; y >= minY; y--) {
                    if (predicate.test(getBlockState(x, y, z))) {
                        heightmap.set(x + z * 16, y - minY);
                        break;
                    }
                }
            }
        }
    }

    private class Section {
        private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.createPalettedContainerCodec(Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState());

        private final NbtCompound nbt;

        private boolean blockStatesLoaded;
        private PalettedContainer<BlockState> blockStateContainer;
        private boolean empty;

        private boolean biomesLoaded;
        private ReadableContainer<RegistryEntry<Biome>> biomeContainer;

        private Section(NbtCompound nbt) {
            this.nbt = nbt;
        }

        public BlockState getBlockState(int x, int y, int z) {
            read();
            return blockStateContainer != null ? blockStateContainer.get(x, y, z) : Blocks.AIR.getDefaultState();
        }

        public boolean isEmpty() {
            read();
            return empty;
        }

        public Biome getBiome(int x, int y, int z) {
            if (!biomesLoaded) {
                if (nbt.contains("biomes")) {
                    biomeContainer = biomeCodec.parse(NbtOps.INSTANCE, nbt.getCompound("biomes")).result().get();
                }

                biomesLoaded = true;
            }

            return biomeContainer != null ? biomeContainer.get(x, y, z).value() : world.getRegistryManager().get(RegistryKeys.BIOME).get(BiomeKeys.PLAINS);
        }

        private void read() {
            if (blockStatesLoaded) return;

            blockStatesLoaded = true;
            empty = true;

            if (nbt.contains("block_states")) {
                blockStateContainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("block_states")).result().get();

                blockStateContainer.count((state, count) -> {
                    if (!state.isAir()) empty = false;
                });
            }
        }
    }
}
