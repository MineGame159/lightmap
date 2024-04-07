package minegame159.lightmap;

import minegame159.lightmap.colors.BiomeColors;
import minegame159.lightmap.commands.Commands;
import minegame159.lightmap.events.ChunkLoadEvent;
import minegame159.lightmap.events.TickEvent;
import minegame159.lightmap.mixininterface.IBiome;
import minegame159.lightmap.mixininterface.IBlockState;
import minegame159.lightmap.mixininterface.IServerWorld;
import minegame159.lightmap.platform.FabricBiome;
import minegame159.lightmap.platform.FabricBlock;
import minegame159.lightmap.platform.FabricPlatform;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
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
        if (firstChunk) {
            onInit(world.getServer());
            firstChunk = false;
        }

        ChunkLoadEvent.invoke(
                ((IServerWorld) world).lightmap$getWorld(),
                chunk.getPos().x,
                chunk.getPos().z
        );
    }

    private void onInit(MinecraftServer server) {
        LightMap.init(new FabricPlatform(server));

        GrassColors.setColorMap(BiomeColors.loadGrassColorMap());
        FoliageColors.setColorMap(BiomeColors.loadFoliageColorMap());

        for (Block block : Registries.BLOCK) {
            for (BlockState state : block.getStateManager().getStates()) {
                ((IBlockState) state).lightmap$setBlock(new FabricBlock(state));
            }
        }

        for (Biome biome : server.getRegistryManager().get(RegistryKeys.BIOME)) {
            ((IBiome) (Object) biome).lightmap$setBiome(new FabricBiome(biome));
        }

        ServerTickEvents.END_SERVER_TICK.register(server1 -> TickEvent.invoke());
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onStop);

        this.server = server;
    }

    private void onStop(MinecraftServer server) {
        LightMap.get().stop();
    }
}
