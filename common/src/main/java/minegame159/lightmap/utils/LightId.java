package minegame159.lightmap.utils;

public record LightId(String namespace, String path) {
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
