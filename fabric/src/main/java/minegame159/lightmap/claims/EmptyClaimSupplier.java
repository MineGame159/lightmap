package minegame159.lightmap.claims;

import minegame159.lightmap.utils.Claim;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class EmptyClaimSupplier implements Supplier<Collection<Claim>> {
    @Override
    public Collection<Claim> get() {
        return List.of();
    }
}
