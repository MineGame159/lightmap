package minegame159.lightmap.claims;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class OpenPACClaimProvider implements ClaimProvider {
    @Override
    public Collection<Claim> getClaims(ServerWorld world) {
        Identifier dimension = world.getDimensionKey().getValue();

        return OpenPACServerAPI.get(world.getServer()).getServerClaimsManager().getPlayerInfoStream()
                .filter(playerInfo -> playerInfo.getDimension(dimension) != null)
                .map(playerInfo -> new Claim(
                        playerInfo.getClaimsName().isBlank() ? playerInfo.getPlayerUsername() + "'s claim" : playerInfo.getClaimsName(),
                        getColor(playerInfo.getClaimsColor()),
                        playerInfo.getDimension(dimension).getStream()
                                .flatMap(IPlayerClaimPosListAPI::getStream)
                                .map(pos -> new Claim.Chunk(pos.x, pos.z))
                                .collect(Collectors.toCollection(ArrayList::new))
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Claim.Color getColor(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;

        return new Claim.Color(r, g, b, a);
    }
}
