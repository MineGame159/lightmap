package minegame159.lightmap.claims;

import minegame159.lightmap.utils.LightId;

import java.util.Collection;

public interface ClaimProvider {
    Collection<Claim> getClaims(LightId world);
}
