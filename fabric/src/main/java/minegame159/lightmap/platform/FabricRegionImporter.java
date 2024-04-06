package minegame159.lightmap.platform;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import minegame159.lightmap.LightMap;
import minegame159.lightmap.LightMapFabric;
import minegame159.lightmap.utils.LightChunkPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.storage.RegionFile;

import java.io.DataInputStream;
import java.io.IOException;

public class FabricRegionImporter implements LightRegionImporter {
    private final int x, z;
    private final RegionFile file;

    private final World world;
    private final Codec<ReadableContainer<RegistryEntry<Biome>>> biomeCodec;

    private final Int2ObjectMap<ChunkHolder> chunks = new Int2ObjectOpenHashMap<>();

    public FabricRegionImporter(int x, int z, RegionFile file) {
        this.x = x;
        this.z = z;
        this.file = file;
        this.world = LightMapFabric.INSTANCE.server.getWorld(World.OVERWORLD);

        Registry<Biome> biomes = world.getRegistryManager().get(RegistryKeys.BIOME);
        biomeCodec = PalettedContainer.createReadableContainerCodec(biomes.getIndexedEntries(), biomes.createEntryCodec(), PalettedContainer.PaletteProvider.BIOME, biomes.entryOf(BiomeKeys.PLAINS));
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public LightChunk getChunk(int x, int z) {
        int pos = (x << 16) | z;

        ChunkHolder holder = chunks.get(pos);
        if (holder != null) return holder.chunk();

        NbtCompound nbt;

        try {
            DataInputStream data = file.getChunkInputStream(new ChunkPos(x, z));

            if (data == null) {
                chunks.put(pos, new ChunkHolder(null));
                return null;
            }

            nbt = NbtIo.read(data);
            data.close();
        } catch (IOException e) {
            LightMap.LOG.error("Failed to read region chunk", e);
            return null;
        }

        LightChunk chunk = new FabricNbtChunk(
                world,
                new LightChunkPos(this.x * 32 + x, this.z * 32 + z),
                biomeCodec,
                nbt
        );

        chunks.put(pos, new ChunkHolder(chunk));
        return chunk;
    }

    @Override
    public void close() {
        try {
            file.close();
        } catch (IOException e) {
            LightMap.LOG.error("Failed to close region file", e);
        }
    }

    private record ChunkHolder(LightChunk chunk) {}
}
