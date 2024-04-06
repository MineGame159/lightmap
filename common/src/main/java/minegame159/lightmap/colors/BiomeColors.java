package minegame159.lightmap.colors;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BiomeColors {
    public static int[] loadGrassColorMap() {
        return loadColorMap("/grass.png");
    }

    public static int[] loadFoliageColorMap() {
        return loadColorMap("/foliage.png");
    }

    private static int[] loadColorMap(String path) {
        try {
            BufferedImage image = ImageIO.read(BiomeColors.class.getResource(path));
            int[] colors = new int[256 * 256];

            for (int x = 0; x < 256; x++) {
                for (int y = 0; y < 256; y++) {
                    colors[y * 256 + x] = toAbgr(image.getRGB(x, y));
                }
            }

            return colors;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int toAbgr(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb) & 0xFF;
        int a = (argb >> 24) & 0xFF;

        return (a << 24) | (b << 16) | (g << 8) | (r);
    }
}
