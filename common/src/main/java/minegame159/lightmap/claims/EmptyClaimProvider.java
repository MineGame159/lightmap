package minegame159.lightmap.claims;

import minegame159.lightmap.utils.LightId;

import java.util.Collection;
import java.util.List;

public class EmptyClaimProvider implements ClaimProvider {
    @Override
    public Collection<Claim> getClaims(LightId world) {
        return List.of();
    }
}
