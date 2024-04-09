package minegame159.lightmap.utils;

public record LightId(String namespace, String path) {
    public static LightId of(String string) {
        String[] splits = string.split(":");

        if (splits.length == 2) return new LightId(splits[0], splits[1]);
        else if (splits.length == 1) return new LightId("minecraft", splits[0]);

        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
