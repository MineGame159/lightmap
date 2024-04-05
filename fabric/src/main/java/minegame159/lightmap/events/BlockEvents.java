package minegame159.lightmap.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class BlockEvents {
    public static final Event<Change> CHANGE = EventFactory.createArrayBacked(
            Change.class,
            listeners -> (chunk, pos) -> {
                for (Change listener : listeners) {
                    listener.onBlockChange(chunk, pos);
                }
            }
    );

    @FunctionalInterface
    public interface Change {
        void onBlockChange(WorldChunk chunk, BlockPos pos);
    }
}
