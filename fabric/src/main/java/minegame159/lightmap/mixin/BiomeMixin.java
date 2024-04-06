package minegame159.lightmap.mixin;

import minegame159.lightmap.mixininterface.IBiome;
import minegame159.lightmap.platform.FabricBiome;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Biome.class)
public abstract class BiomeMixin implements IBiome {
    @Shadow
    protected abstract int getDefaultGrassColor();

    @Unique
    private FabricBiome lightBiome;

    @Override
    public int lightmap$getDefaultGrassColor() {
        return getDefaultGrassColor();
    }

    @Override
    public FabricBiome lightmap$getBiome() {
        return lightBiome;
    }

    @Override
    public void lightmap$setBiome(FabricBiome biome) {
        lightBiome = biome;
    }
}
