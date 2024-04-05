package minegame159.lightmap.claims;

import net.minecraft.server.world.ServerWorld;

import java.util.Collection;

public interface ClaimProvider {
    Collection<Claim> getClaims(ServerWorld world);
}
