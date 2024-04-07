package minegame159.lightmap.events;

import java.util.ArrayList;
import java.util.List;

public class TickEvent {
    private static final List<Callback> callbacks = new ArrayList<>();

    public static synchronized void register(Callback callback) {
        callbacks.add(callback);
    }

    public static synchronized void invoke() {
        for (Callback callback : callbacks) {
            callback.onTick();
        }
    }

    public interface Callback {
        void onTick();
    }
}
