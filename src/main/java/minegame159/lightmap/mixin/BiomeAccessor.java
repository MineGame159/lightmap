package minegame159.lightmap.mixin;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.class)
public interface BiomeAccessor {
    @Invoker("getDefaultGrassColor")
    int lightmap$getDefaultGrassColor();

    @Invoker("getDefaultFoliageColor")
    int lightmap$getDefaultFoliageColor();
}
