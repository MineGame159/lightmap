package minegame159.lightmap.platform;

import minegame159.lightmap.claims.EmptyClaimSupplier;
import minegame159.lightmap.claims.OpenPACClaimSupplier;
import minegame159.lightmap.mixin.MinecraftServerAccessor;
import minegame159.lightmap.utils.Claim;
import minegame159.lightmap.utils.LightId;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Supplier;

public class FabricWorld implements LightWorld {
    private final LightId id;
    private final ServerWorld world;
    private final Supplier<Collection<Claim>> claimSupplier;

    public FabricWorld(LightId id, ServerWorld world) {
        this.id = id;
        this.world = world;

        if (FabricLoader.getInstance().isModLoaded("openpartiesandclaims")) {
            claimSupplier = new OpenPACClaimSupplier(world.getServer(), world.getRegistryKey().getValue());
        }
        else {
            claimSupplier = new EmptyClaimSupplier();
        }
    }

    @Override
    public LightId getId() {
        return id;
    }

    @Override
    public Path getFolder() {
        return ((MinecraftServerAccessor) world.getServer()).light$session().getWorldDirectory(RegistryKey.of(RegistryKeys.WORLD, new Identifier(id.namespace(), id.path())));
    }

    @Override
    public LightChunk getChunk(int x, int z) {
        Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, false);
        return chunk != null ? new FabricChunk(this, chunk) : null;
    }

    @Override
    public Collection<Claim> getClaims() {
        return claimSupplier.get();
    }
}
