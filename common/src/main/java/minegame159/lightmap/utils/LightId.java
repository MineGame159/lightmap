package minegame159.lightmap.utils;

public record LightId(String namespace, String path) {
    public static LightId of(String string) {
        String[] splits = string.split(":");
        return new LightId(splits[0], splits[1]);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
