package minegame159.lightmap.mixin;

import minegame159.lightmap.mixininterface.IServerWorld;
import minegame159.lightmap.platform.FabricWorld;
import minegame159.lightmap.platform.LightWorld;
import minegame159.lightmap.utils.LightId;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements IServerWorld {
    @Unique
    private LightWorld lightWorld;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Override
    public LightWorld lightmap$getWorld() {
        if (lightWorld == null) {
            Identifier id = getRegistryKey().getValue();
            lightWorld = new FabricWorld(new LightId(id.getNamespace(), id.getPath()), (ServerWorld) (Object) this);
        }

        return lightWorld;
    }
}
