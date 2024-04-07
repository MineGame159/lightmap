package minegame159.lightmap.claims;

import minegame159.lightmap.utils.Claim;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OpenPACClaimSupplier implements Supplier<Collection<Claim>> {
    private final MinecraftServer server;
    private final Identifier worldId;

    public OpenPACClaimSupplier(MinecraftServer server, Identifier worldId) {
        this.server = server;
        this.worldId = worldId;
    }

    @Override
    public Collection<Claim> get() {
        return OpenPACServerAPI.get(server).getServerClaimsManager().getPlayerInfoStream()
                .filter(playerInfo -> playerInfo.getDimension(worldId) != null)
                .map(playerInfo -> new Claim(
                        playerInfo.getClaimsName().isBlank() ? playerInfo.getPlayerUsername() + "'s claim" : playerInfo.getClaimsName(),
                        getColor(playerInfo.getClaimsColor()),
                        playerInfo.getDimension(worldId).getStream()
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
