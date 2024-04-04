package minegame159.lightmap.textures;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public class BlockColors {
    private static final TextureResolver resolver = new TextureResolver();

    private static final Object2IntMap<Block> blockColors = new Object2IntLinkedOpenHashMap<>();
    private static final Object2IntMap<Identifier> idColors = new Object2IntLinkedOpenHashMap<>();

    public static int get(Block block) {
        int color = blockColors.getOrDefault(block, -1);

        if (color == -1) {
            color = calculate(Registries.BLOCK.getId(block));
            blockColors.put(block, color);
        }

        return color;
    }

    public static int get(Identifier id) {
        int color = idColors.getOrDefault(id, -1);

        if (color == -1) {
            color = calculate(id);
            idColors.put(id, color);
        }

        return color;
    }

    private static int calculate(Identifier id) {
        BufferedImage image = resolver.get(id);
        if (image == null) return 0;

        double count = getTransparentPercentage(image);

        if (count <= 0.5) return average(image);
        return topWeightedAverage(image);
    }

    private static double getTransparentPercentage(BufferedImage image) {
        int count = 0;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);

                int pixelA = (pixel >> 24) & 0xFF;
                if (pixelA < 10) count++;
            }
        }

        return ((double) count) / (image.getWidth() * image.getHeight());
    }

    private static int average(BufferedImage image) {
        int r = 0;
        int g = 0;
        int b = 0;
        int a = 0;

        int count = 0;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);

                int pixelA = (pixel >> 24) & 0xFF;
                if (pixelA < 10) continue;

                r += (pixel >> 16) & 0xFF;
                g += (pixel >> 8) & 0xFF;
                b += (pixel) & 0xFF;
                a += pixelA;

                count++;
            }
        }

        int transparentCount = (image.getWidth() * image.getHeight() - count);
        r += transparentCount * 255;
        g += transparentCount * 255;
        b += transparentCount * 255;
        a += transparentCount * 255;
        count += transparentCount;

        r /= count;
        g /= count;
        b /= count;
        a /= count;

        return (a << 24) | (b << 16) | (g << 8) | (r);
    }

    private static int topWeightedAverage(BufferedImage image) {
        // Bounds
        int minX = image.getWidth();
        int minY = image.getHeight();

        int maxX = 0;
        int maxY = 0;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);

                int pixelA = (pixel >> 24) & 0xFF;
                if (pixelA < 10) continue;

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);

                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        int height = maxY - minY;

        // Average
        int r = 0;
        int g = 0;
        int b = 0;
        int a = 0;

        int count = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                int pixel = image.getRGB(x, y);

                int pixelA = (pixel >> 24) & 0xFF;
                if (pixelA < 10) continue;

                int factor = getWeightedFactor(y - minY, height);

                r += ((pixel >> 16) & 0xFF) * factor;
                g += ((pixel >> 8) & 0xFF) * factor;
                b += ((pixel) & 0xFF) * factor;
                a += pixelA * factor;

                count += factor;
            }
        }

        r /= count;
        g /= count;
        b /= count;
        a /= count;

        return (a << 24) | (b << 16) | (g << 8) | (r);
    }

    private static int getWeightedFactor(int y, int height) {
        double percentage = (double) y / height;

        if (percentage > 0.75) return 0;
        if (percentage > 0.5) return 1;
        if (percentage > 0.25) return 2;
        return 3;
    }

    public static void close() {
        resolver.close();
    }
}
