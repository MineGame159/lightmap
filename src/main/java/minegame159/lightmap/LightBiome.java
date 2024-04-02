package minegame159.lightmap;

import minegame159.lightmap.mixin.BiomeAccessor;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LightBiome {
    private final int grassColor;
    private final BiomeEffects.GrassColorModifier grassModifier;

    private final int foliageColor;
    private final int waterColor;

    private LightBiome(Biome biome) {
        Optional<Integer> grassOptional = biome.getEffects().getGrassColor();
        grassColor = grassOptional.isPresent() ? grassOptional.get() : ((BiomeAccessor) (Object) biome).lightmap$getDefaultGrassColor();
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

    // Static

    private static final Map<Biome, LightBiome> MAP = new HashMap<>();

    public static LightBiome get(Biome biome) {
        return MAP.get(biome);
    }

    public static void init(MinecraftServer server) {
        for (Biome biome : server.getRegistryManager().get(RegistryKeys.BIOME)) {
            MAP.put(biome, new LightBiome(biome));
        }
    }
}
