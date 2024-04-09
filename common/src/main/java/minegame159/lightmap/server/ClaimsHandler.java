package minegame159.lightmap.server;

import minegame159.lightmap.LightMap;
import minegame159.lightmap.platform.LightWorld;
import minegame159.lightmap.utils.Claim;
import minegame159.lightmap.utils.LightId;
import org.microhttp.Response;

import java.util.Collection;
import java.util.List;

public class ClaimsHandler {
    public Response get(LightId worldId) {
        LightWorld world = LightMap.get().getPlatform().getWorld(worldId);
        Collection<Claim> claims = world != null ? world.getClaims() : List.of();

        return HttpUtils.newJsonResponse(200, claims);
    }
}
