package minegame159.lightmap.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class LightMapEvents {
    public static final Event<RegionUpdate> REGION_UPDATE = EventFactory.createArrayBacked(
            RegionUpdate.class,
            listeners -> (x, z) -> {
                for (RegionUpdate listener : listeners) {
                    listener.onRegionUpdate(x, z);
                }
            }
    );

    @FunctionalInterface
    public interface RegionUpdate {
        void onRegionUpdate(int x, int z);
    }
}
