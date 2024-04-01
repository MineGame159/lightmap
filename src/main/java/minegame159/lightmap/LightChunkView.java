package minegame159.lightmap;

import java.awt.image.BufferedImage;

public class LightChunkView {
    private final BufferedImage image;
    private final int startX, startZ;

    public LightChunkView(BufferedImage image, int startX, int startZ) {
        this.image = image;
        this.startX = startX;
        this.startZ = startZ;
    }

    public void set(int x, int z, int argb) {
        image.setRGB(startX + x, startZ + z, argb);
    }
}
