package minegame159.lightmap.importer;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import minegame159.lightmap.LightMap;
import minegame159.lightmap.LightWorld;
import minegame159.lightmap.RenderTask;
import minegame159.lightmap.task.Task;
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
import java.io.File;
import java.io.IOException;
import java.util.List;

public class WorldImporter {
    private final World world;
    private final File directory;
    private final File[] files;

    private final Codec<ReadableContainer<RegistryEntry<Biome>>> biomeCodec;

    private int i;
    private int regionX, regionZ;

    public WorldImporter(World world, File directory) {
        this.world = world;
        this.directory = directory;
        this.files = directory.listFiles();

        Registry<Biome> biomes = world.getRegistryManager().get(RegistryKeys.BIOME);
        biomeCodec = PalettedContainer.createReadableContainerCodec(biomes.getIndexedEntries(), biomes.createEntryCodec(), PalettedContainer.PaletteProvider.BIOME, biomes.entryOf(BiomeKeys.PLAINS));
    }

    public int finishedCount() {
        return i;
    }

    public int count() {
        return files.length;
    }

    public boolean hasMore() {
        return i < files.length;
    }

    public ImportRegionTask getImportRegionTask() {
        return new ImportRegionTask(this);
    }

    void importRegion(List<Task> tasks) {
        Int2ObjectMap<RenderTask> map = createRenderTasks();
        if (map == null) return;

        for (int pos : map.keySet()) {
            int x = getPosX(pos);
            int z = getPosZ(pos);

            RenderTask task = map.get(pos);

            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    int neighbourX = x + offsetX;
                    int neighbourZ = z + offsetZ;

                    if (neighbourX < 0 || neighbourX >= 32 || neighbourZ < 0 || neighbourZ >= 32) {
                        continue;
                    }

                    RenderTask neighbour = map.get(packPos(neighbourX, neighbourZ));
                    task.setChunk(offsetX, offsetZ, neighbour != null ? neighbour.getCurrent() : null);
                }
            }
        }

        tasks.addAll(map.values());
        tasks.add(new FlushRegionTask(regionX, regionZ));
    }

    private Int2ObjectMap<RenderTask> createRenderTasks() {
        File file = getRegionFile();
        if (file == null) return null;

        Int2ObjectMap<RenderTask> tasks = new Int2ObjectOpenHashMap<>();
        RegionFile region;

        try {
            region = new RegionFile(file.toPath(), directory.toPath(), false);
        }
        catch (IOException e) {
            LightMap.LOG.error("Failed to read region at [{}, {}]", regionX, regionZ);
            return null;
        }

        for (int chunkX = 0; chunkX < 32; chunkX++) {
            for (int chunkZ = 0; chunkZ < 32; chunkZ++) {
                NbtCompound nbt;

                try {
                    DataInputStream data = region.getChunkInputStream(new ChunkPos(chunkX, chunkZ));
                    if (data == null) continue;

                    nbt = NbtIo.read(data);
                    data.close();
                }
                catch (IOException e) {
                    LightMap.LOG.error("Failed to read chunk at [{}, {}] in region at [{}, {}]", chunkX, chunkZ, regionX, regionZ);
                    continue;
                }

                LightWorld world = LightMap.getInstance().getWorld();
                ChunkPos pos = new ChunkPos(regionX * 32 + chunkX, regionZ * 32 + chunkZ);

                world.getRegion(pos).addPending();
                tasks.put(packPos(chunkX, chunkZ), new RenderTask(world, new NbtChunk(this.world, pos, biomeCodec, nbt)));
            }
        }

        try {
            region.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tasks;
    }

    private File getRegionFile() {
        while (i < files.length) {
            File file = files[i++];
            if (!file.isFile() || !file.getName().endsWith(".mca")) continue;

            String[] splits = file.getName().split("\\.");
            if (splits.length != 4) continue;

            try {
                regionX = Integer.parseInt(splits[1]);
                regionZ = Integer.parseInt(splits[2]);
            }
            catch (NumberFormatException ignored) {
                continue;
            }

            return file;
        }

        return null;
    }

    private static int packPos(int x, int z) {
        return (x << 6) | (z);
    }

    private static int getPosX(int pos) {
        return pos >> 6;
    }
    private static int getPosZ(int pos) {
        return pos & 31;
    }

    private static class FlushRegionTask extends Task {
        private final int x, z;

        private FlushRegionTask(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        protected void runImpl() {
            LightMap.getInstance().getWorld().flushAndClose(x, z);
        }
    }
}
