package minegame159.lightmap.platform;

import minegame159.lightmap.utils.Claim;
import minegame159.lightmap.utils.LightId;

import java.nio.file.Path;
import java.util.Collection;

public interface LightWorld {
    LightId getId();

    Path getFolder();

    LightChunk getChunk(int x, int z);

    Collection<Claim> getClaims();
}
