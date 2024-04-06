package minegame159.lightmap.claims;

public class ClaimProviders {
    private static ClaimProvider provider = new EmptyClaimProvider();

    public static void set(ClaimProvider provider) {
        if (!(ClaimProviders.provider instanceof EmptyClaimProvider)) {
            throw new IllegalStateException("Claim Provider was already set previously");
        }

        ClaimProviders.provider = provider;
    }

    public static ClaimProvider get() {
        return provider;
    }
}
