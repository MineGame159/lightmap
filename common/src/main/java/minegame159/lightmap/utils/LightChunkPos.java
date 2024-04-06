package minegame159.lightmap.utils;

public record LightChunkPos(int x, int z) {
    public long toLong() {
        return (((long) z & 4294967295L) << 32) | ((long) x & 4294967295L);
    }
}
