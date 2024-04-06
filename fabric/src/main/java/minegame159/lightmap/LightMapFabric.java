package minegame159.lightmap;

import minegame159.lightmap.claims.ClaimProviders;
import minegame159.lightmap.claims.OpenPACClaimProvider;
import minegame159.lightmap.colors.BiomeColors;
import minegame159.lightmap.commands.Commands;
import minegame159.lightmap.mixininterface.IBiome;
import minegame159.lightmap.mixininterface.IBlockState;
import minegame159.lightmap.platform.FabricBiome;
import minegame159.lightmap.platform.FabricBlock;
import minegame159.lightmap.platform.FabricChunk;
import minegame159.lightmap.platform.FabricPlatform;
import minegame159.lightmap.utils.LightChunkPos;
import minegame159.lightmap.utils.LightId;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

public class LightMapFabric implements DedicatedServerModInitializer {
    public static LightMapFabric INSTANCE;

    private boolean firstChunk = true;

    public MinecraftServer server;

    @Override
    public void onInitializeServer() {
        INSTANCE = this;

        Commands.register();

        ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
    }

    private void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        if (world.getRegistryKey() != World.OVERWORLD) return;

        if (firstChunk) {
            Identifier id = world.getRegistryKey().getValue();

            LightMap.init(
                    new FabricPlatform(world.getServer()),
                    new LightId(id.getNamespace(), id.getPath())
            );

            GrassColors.setColorMap(BiomeColors.loadGrassColorMap());
            FoliageColors.setColorMap(BiomeColors.loadFoliageColorMap());

            for (Block block : Registries.BLOCK) {
                for (BlockState state : block.getStateManager().getStates()) {
                    ((IBlockState) state).lightmap$setBlock(new FabricBlock(state));
                }
            }

            for (Biome biome : world.getRegistryManager().get(RegistryKeys.BIOME)) {
                ((IBiome) (Object) biome).lightmap$setBiome(new FabricBiome(biome));
            }

            if (FabricLoader.getInstance().isModLoaded("openpartiesandclaims")) {
                ClaimProviders.set(new OpenPACClaimProvider(world.getServer()));
            }

            ServerTickEvents.END_SERVER_TICK.register(this::onTick);
            ServerLifecycleEvents.SERVER_STOPPING.register(this::onStop);

            firstChunk = false;
            server = world.getServer();
        }

        if (!LightMap.get().getWorld().isChunkRendered(new LightChunkPos(chunk.getPos().x, chunk.getPos().z))) {
            LightMap.get().markAsPending(new FabricChunk(chunk));
        }
    }

    private void onTick(MinecraftServer server) {
        LightMap.get().tick();
    }

    private void onStop(MinecraftServer server) {
        LightMap.get().stop();
    }
}
