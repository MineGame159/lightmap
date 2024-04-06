package minegame159.lightmap.claims;

import java.util.Collection;

public record Claim(String name, Color color, Collection<Chunk> chunks) {
    public record Color(int r, int g, int b, int a) {}
    public record Chunk(int x, int z) {}
}
