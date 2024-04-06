package minegame159.lightmap;

import meteordevelopment.nbt.NBT;
import meteordevelopment.nbt.NbtFormatException;
import meteordevelopment.nbt.tags.CompoundTag;
import minegame159.lightmap.task.Task;
import minegame159.lightmap.task.TaskQueue;
import minegame159.lightmap.utils.LightChunkPos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;

public class LightRegion {
    public static final int SIZE = 32;

    private final File directory;

    private int x, z;
    private BitSet chunks;

    private BufferedImage image;

    private int pendingModifications;
    private long modifiedTime;

    public LightRegion(File directory, int x, int z) {
        this.directory = directory;
        this.x = x;
        this.z = z;
        this.chunks = new BitSet(SIZE * SIZE);

        readMetadata();
    }

    public synchronized boolean containsChunk(LightChunkPos pos) {
        return chunks.get(getChunkIndex(pos));
    }

    public LightChunkView getView(LightChunkPos pos) {
        if (image == null) {
            readImage();
        }

        return new LightChunkView(
                image,
                (pos.x() & (SIZE - 1)) * 16,
                (pos.z() & (SIZE - 1)) * 16
        );
    }

    public void addPending() {
        pendingModifications++;
    }

    public synchronized void markChunk(LightChunkPos pos) {
        chunks.set(getChunkIndex(pos));

        pendingModifications--;
        modifiedTime = System.nanoTime();
    }

    public synchronized void scheduleWrite(TaskQueue tasks) {
        if (modifiedTime == 0) return;
        if (System.nanoTime() - modifiedTime <= 500L * 1000000L) return;
        if (pendingModifications != 0) return;

        tasks.add(new WriteTask());
    }

    public synchronized void write() {
        writeMetadata();
        writeImage();

        modifiedTime = 0;
    }

    private int getChunkIndex(LightChunkPos pos) {
        int x = pos.x() & (SIZE - 1);
        int z = pos.z() & (SIZE - 1);

        return z * SIZE + x;
    }

    private class WriteTask extends Task {
        @Override
        protected void runImpl() {
            write();
        }
    }

    // Image IO

    private void readImage() {
        File file = new File(directory, "r_" + x + "_" + z + ".png");

        if (file.exists()) {
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            image = new BufferedImage(SIZE * 16, SIZE * 16, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private void writeImage() {
        File file = new File(directory, "r_" + x + "_" + z + ".png");

        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Metadata IO

    private void readMetadata() {
        File file = new File(directory, "r_" + x + "_" + z + ".dat");

        if (file.exists()) {
            try {
                readNbt(NBT.read(file).tag);
            } catch (NbtFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeMetadata() {
        File file = new File(directory, "r_" + x + "_" + z + ".dat");

        CompoundTag nbt = writeNbt();
        NBT.write(nbt, file);
    }

    private CompoundTag writeNbt() {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("X", x);
        nbt.putInt("Z", z);

        nbt.putLongArray("Chunks", chunks.toLongArray());

        return nbt;
    }

    private void readNbt(CompoundTag nbt) {
        x = nbt.getInt("X");
        z = nbt.getInt("Z");

        chunks = BitSet.valueOf(nbt.getLongArray("Chunks"));
    }

    // Position helpers

    public static int getRegionX(LightChunkPos pos) {
        return pos.x() >> 5;
    }
    public static int getRegionZ(LightChunkPos pos) {
        return pos.z() >> 5;
    }

    public static long getRegionPos(LightChunkPos pos) {
        return getRegionPos(getRegionX(pos), getRegionZ(pos));
    }
    public static long getRegionPos(int x, int z) {
        return ((x & 0xFFFFFFFFL) << 32) | (z & 0xFFFFFFFFL);
    }
}
