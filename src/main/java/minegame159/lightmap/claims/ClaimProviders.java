package minegame159.lightmap.claims;

import net.fabricmc.loader.api.FabricLoader;

public class ClaimProviders {
    private static ClaimProvider provider;

    public static ClaimProvider get() {
        return provider;
    }

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("openpartiesandclaims")) {
            provider = new OpenPACClaimProvider();
        }
        else {
            provider = new EmptyClaimProvider();
        }
    }
}
