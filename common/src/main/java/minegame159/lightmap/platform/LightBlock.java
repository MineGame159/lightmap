package minegame159.lightmap.platform;

import minegame159.lightmap.utils.LightId;

public interface LightBlock {
    LightId getId();

    boolean isAir();
    boolean isWater();

    boolean hasCollisions();

    int getMapColor();
}
