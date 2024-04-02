package minegame159.lightmap.claims;

import net.minecraft.server.world.ServerWorld;

import java.util.Collection;
import java.util.List;

public class EmptyClaimProvider implements ClaimProvider {
    @Override
    public Collection<Claim> getClaims(ServerWorld world) {
        return List.of();
    }
}
