package minegame159.lightmap.models;

public class VersionMeta {
    public Downloads downloads;

    public static class Downloads {
        public Download client;
    }

    public static class Download {
        public String url;
    }
}
