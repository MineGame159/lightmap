package minegame159.lightmap.platform;

import minegame159.lightmap.utils.LightId;

import java.nio.file.Path;

public interface LightPlatform {
    String getMcVersion();

    Path getCacheFolder();
    Path getDataFolder();

    LightWorld getWorld(LightId id);

    LightRegionImporter createRegionImporter(Path path);
}
