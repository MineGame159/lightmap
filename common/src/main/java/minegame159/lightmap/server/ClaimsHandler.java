package minegame159.lightmap.server;

import minegame159.lightmap.LightMap;
import minegame159.lightmap.claims.ClaimProviders;
import minegame159.lightmap.utils.LightId;
import org.microhttp.Response;

public class ClaimsHandler {
    public Response get() {
        LightId id = LightMap.get().getWorld().getId();
        return HttpUtils.newJsonResponse(200, ClaimProviders.get().getClaims(id));
    }
}
