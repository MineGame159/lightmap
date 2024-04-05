package minegame159.lightmap.server;

import minegame159.lightmap.LightMap;
import minegame159.lightmap.claims.ClaimProviders;
import org.microhttp.Response;

public class ClaimsHandler {
    public Response get() {
        return HttpUtils.newJsonResponse(200, ClaimProviders.get().getClaims(LightMap.getInstance().world.world));
    }
}
