package minegame159.lightmap.platform;

public interface LightRegionImporter {
    int getX();
    int getZ();

    LightChunk getChunk(int x, int z);

    void close();
}
