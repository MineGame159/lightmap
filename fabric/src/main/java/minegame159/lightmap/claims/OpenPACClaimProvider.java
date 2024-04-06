package minegame159.lightmap.claims;

import minegame159.lightmap.utils.LightId;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class OpenPACClaimProvider implements ClaimProvider {
    private final MinecraftServer server;

    public OpenPACClaimProvider(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Collection<Claim> getClaims(LightId world) {
        Identifier id = new Identifier(world.namespace(), world.path());

        return OpenPACServerAPI.get(server).getServerClaimsManager().getPlayerInfoStream()
                .filter(playerInfo -> playerInfo.getDimension(id) != null)
                .map(playerInfo -> new Claim(
                        playerInfo.getClaimsName().isBlank() ? playerInfo.getPlayerUsername() + "'s claim" : playerInfo.getClaimsName(),
                        getColor(playerInfo.getClaimsColor()),
                        playerInfo.getDimension(id).getStream()
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
