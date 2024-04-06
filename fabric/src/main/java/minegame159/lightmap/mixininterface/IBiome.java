package minegame159.lightmap.mixininterface;

import minegame159.lightmap.platform.FabricBiome;

public interface IBiome {
    int lightmap$getDefaultGrassColor();

    FabricBiome lightmap$getBiome();

    void lightmap$setBiome(FabricBiome biome);
}
