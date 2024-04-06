package minegame159.lightmap.platform;

import minegame159.lightmap.mixininterface.IBiome;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;

import java.util.Optional;

public class FabricBiome implements LightBiome {
    private final int grassColor;
    private final BiomeEffects.GrassColorModifier grassModifier;

    private final int foliageColor;
    private final int waterColor;

    public FabricBiome(Biome biome) {
        Optional<Integer> grassOptional = biome.getEffects().getGrassColor();
        grassColor = grassOptional.isPresent() ? grassOptional.get() : ((IBiome) (Object) biome).lightmap$getDefaultGrassColor();
        grassModifier = biome.getEffects().getGrassColorModifier();

        foliageColor = biome.getFoliageColor();
        waterColor = waterColorToAbgr(biome.getWaterColor());
    }

    private static int waterColorToAbgr(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;
        int a = (color >> 24) & 0xFF;

        return (a << 24) | (b << 16) | (g << 8) | (r);
    }

    public int getGrassColor(int x, int z) {
        return grassModifier.getModifiedGrassColor(x, z, grassColor);
    }

    public int getFoliageColor() {
        return foliageColor;
    }

    public int getWaterColor() {
        return waterColor;
    }
}
