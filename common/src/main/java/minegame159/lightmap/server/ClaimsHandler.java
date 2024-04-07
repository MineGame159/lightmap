package minegame159.lightmap.server;

import minegame159.lightmap.LightMap;
import minegame159.lightmap.platform.LightWorld;
import minegame159.lightmap.utils.LightId;
import org.microhttp.Response;

public class ClaimsHandler {
    public Response get() {
        LightWorld world = LightMap.get().getPlatform().getWorld(new LightId("minecraft", "overworld"));
        return HttpUtils.newJsonResponse(200, world.getClaims());
    }
}
