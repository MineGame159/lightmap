package minegame159.lightmap.events;

import minegame159.lightmap.platform.LightWorld;

import java.util.ArrayList;
import java.util.List;

public class ChunkLoadEvent {
    private static final List<Callback> callbacks = new ArrayList<>();

    public static synchronized void register(Callback callback) {
        callbacks.add(callback);
    }

    public static synchronized void invoke(LightWorld world, int x, int z) {
        for (Callback callback : callbacks) {
            callback.onChunkUpdate(world, x, z);
        }
    }

    public interface Callback {
        void onChunkUpdate(LightWorld world, int x, int z);
    }
}
