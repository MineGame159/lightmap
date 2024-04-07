package minegame159.lightmap.platform;

import minegame159.lightmap.LightMap;
import minegame159.lightmap.mixin.MinecraftServerAccessor;
import minegame159.lightmap.mixininterface.IServerWorld;
import minegame159.lightmap.utils.LightId;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.storage.RegionFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FabricPlatform implements LightPlatform {
    private final MinecraftServer server;

    private final Map<LightId, WorldHolder> worlds = new HashMap<>();

    public FabricPlatform(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public String getMcVersion() {
        return SharedConstants.getGameVersion().getName();
    }

    @Override
    public Path getCacheFolder() {
        return FabricLoader.getInstance().getGameDir().resolve(".lightmap-cache");
    }

    @Override
    public Path getDataFolder() {
        return ((MinecraftServerAccessor) server).light$session().getWorldDirectory(World.OVERWORLD).resolve("data").resolve("lightmap");
    }

    @Override
    public LightWorld getWorld(LightId id) {
        WorldHolder holder = worlds.get(id);
        if (holder != null) return holder.world();

        ServerWorld mcWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(id.namespace(), id.path())));
        if (mcWorld == null) {
            worlds.put(id, new WorldHolder(null));
            return null;
        }

        LightWorld world = ((IServerWorld) mcWorld).lightmap$getWorld();
        worlds.put(id, new WorldHolder(world));

        return world;
    }

    @Override
    public LightRegionImporter createRegionImporter(Path path) {
        String[] splits = path.getFileName().toString().split("\\.");

        if (splits.length != 4) {
            LightMap.LOG.error("Invalid region file name");
            return null;
        }

        int x;
        int z;

        try {
            x = Integer.parseInt(splits[1]);
            z = Integer.parseInt(splits[2]);
        } catch (NumberFormatException e) {
            LightMap.LOG.error("Invalid region file name", e);
            return null;
        }

        try {
            return new FabricRegionImporter(x, z, new RegionFile(path, path.getParent(), false));
        } catch (IOException e) {
            LightMap.LOG.error("Failed to open region file", e);
            return null;
        }
    }

    private record WorldHolder(LightWorld world) {}
}
