package minegame159.lightmap;

import minegame159.lightmap.colors.BlockColors;
import minegame159.lightmap.platform.LightBiome;
import minegame159.lightmap.platform.LightBlock;
import minegame159.lightmap.platform.LightChunk;
import minegame159.lightmap.utils.Brightness;
import minegame159.lightmap.utils.LightChunkPos;
import minegame159.lightmap.utils.LightId;

import java.util.HashMap;
import java.util.Map;

public class ChunkRenderer {
    private static class Column {
        private int topY;
        private LightBlock topBlock;

        private int collidableY;
        private LightBlock collidableBlock;
    }

    private static final Column[] columns = new Column[16 * 17];
    private static final LightBiome[] biomes = new LightBiome[6 * 6];

    static {
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new Column();
        }
    }

    private static Column getColumn(int x, int z) {
        return columns[z * 16 + x];
    }

    private static LightBiome getBiome(int x, int z) {
        return biomes[((z >> 2) + 1) * 6 + ((x >> 2) + 1)];
    }

    private static void setBiome(int x, int z, LightBiome biome) {
        biomes[((z >> 2) + 1) * 6 + ((x >> 2) + 1)] = biome;
    }

    public static void render(RenderTask task) {
        fillColumns(task);
        fillBiomes(task);

        // Render
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Column current = getColumn(x, z);
                if (current.topY != -1) renderColumn(task, x,  z, current);
            }
        }
    }

    private static void fillColumns(RenderTask task) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                fillColumn(task.getCurrent(), x, z, getColumn(x, z));
            }
        }

        if (task.getChunk(0, 1) != null) {
            for (int x = 0; x < 16; x++) {
                fillColumn(task.getChunk(0, 1), x, 0, getColumn(x, 16));
            }
        }
        else {
            for (int x = 0; x < 16; x++) {
                Column column = getColumn(x, 16);

                column.topY = Integer.MIN_VALUE;
                column.collidableY = Integer.MIN_VALUE;
            }
        }
    }

    private static void fillColumn(LightChunk chunk, int x, int z, Column column) {
        // Top
        int y = chunk.getTopY(x, z);
        LightBlock block = chunk.getBlock(x, y, z);

        while (block.isAir()) {
            if (y <= chunk.getBottomY()) {
                y = -1;
                break;
            }

            y--;
            block = chunk.getBlock(x, y, z);
        }

        column.topY = y;
        column.topBlock = block;

        // Collidable
        if (block.isWater()) {
            while (!block.hasCollisions()) {
                if (y <= chunk.getBottomY()) {
                    y = -1;
                    break;
                }

                y--;
                block = chunk.getBlock(x, y, z);
            }
        }

        column.collidableY = y;
        column.collidableBlock = block;
    }

    private static void fillBiomes(RenderTask task) {
        for (int x = -4; x < 20; x += 4) {
            for (int z = -4; z < 20; z += 4) {
                int cx = 0;
                int cz = 0;

                if (x < 0) cx = -1;
                else if (x >= 16) cx = 1;

                if (z < 0) cz = -1;
                else if (z >= 16) cz = 1;

                int rx = x & 15;
                int rz = z & 15;

                LightChunk chunk = task.getChunk(cx, cz);
                LightBiome biome;

                if (x == rx && z == rz) {
                    Column column = getColumn(rx, rz);
                    biome = chunk.getBiome(rx, column.topY, rz);
                }
                else if (chunk != null) {
                    biome = chunk.getBiome(rx, chunk.getTopY(rx, rz), rz);
                }
                else {
                    rx = (rx - cx * 4) & 15;
                    rz = (rx - cz * 4) & 15;

                    Column column = getColumn(rx, rz);
                    biome = task.getCurrent().getBiome(rx, column.topY, rz);
                }

                setBiome(x, z, biome);
            }
        }
    }

    private static void renderColumn(RenderTask task, int x, int z, Column current) {
        LightChunk chunk = task.getCurrent();

        if (current.topBlock.isWater()) {
            // Water
            int depth = current.topY - current.collidableY;

            int color = getColor(chunk, current.topBlock, x, current.topY, z);
            int underColor = getColor(chunk, current.collidableBlock, x, current.collidableY, z);

            int abgr = mix(color, underColor, 0.15f / (depth / 2f));
            task.set(x, z, toArgb(abgr, Brightness.Normal));
        }
        else {
            // Other
            Brightness brightness = Brightness.Normal;

            if (z + 1 < 17) {
                Column above = getColumn(x, z + 1);

                if (above.topY != Integer.MIN_VALUE) {
                    if (above.topY > current.topY) brightness = Brightness.High;
                    else if (above.topY < current.topY) brightness = Brightness.Low;
                }
            }

            int abgr = getColor(chunk, current.topBlock, x, current.topY, z);
            if (abgr != 0) task.set(x, z, toArgb(abgr, brightness));
        }
    }

    private static final Map<LightId, ColorProvider> COLOR_PROVIDERS = new HashMap<>();

    private static int getColor(LightChunk chunk, LightBlock block, int x, int y, int z) {
        LightId id = block.getId();
        if (block.isWater()) id = WATER;

        ColorProvider provider = COLOR_PROVIDERS.get(id);
        if (provider != null) {
            int color = provider.get(chunk, block, x, y, z);

            if (color >> 24 == 0) {
                return (0xFF << 24) | color;
            }

            return color;
        }

        int color = BlockColors.get(block);
        if (color != 0) return color;

        return block.getMapColor();
    }

    private enum BiomeColorType {
        Grass,
        Foliage,
        Water
    }

    private static int getAverageBiomeColor(LightChunkPos pos, BiomeColorType type, int x, int z) {
        int r = 0;
        int g = 0;
        int b = 0;

        final int count = 9 * 9;

        for (int offsetX = -4; offsetX <= 4; offsetX++) {
            for (int offsetZ = -4; offsetZ <= 4; offsetZ++) {
                LightBiome biome = getBiome(x + offsetX, z + offsetZ);

                switch (type) {
                    case Grass -> {
                        int color = biome.getGrassColor(pos.x() * 16 + x, pos.z() * 16 + z);

                        r += (color) & 0xFF;
                        g += (color >> 8) & 0xFF;
                        b += (color >> 16) & 0xFF;
                    }
                    case Foliage -> {
                        int color = biome.getFoliageColor();

                        r += (color) & 0xFF;
                        g += (color >> 8) & 0xFF;
                        b += (color >> 16) & 0xFF;
                    }
                    case Water -> {
                        int color = biome.getWaterColor();

                        r += ((color) & 0xFF) * 190 / 255;
                        g += ((color >> 8) & 0xFF) * 190 / 255;
                        b += ((color >> 16) & 0xFF) * 190 / 255;
                    }
                }
            }
        }

        r /= count;
        g /= count;
        b /= count;

        return (255 << 24) | (b << 16) | (g << 8) | (r);
    }

    private static final LightId GRASS_BLOCK_TOP = new LightId("minecraft", "grass_block_top");
    private static final LightId GRASS = new LightId("minecraft", "grass");
    private static final LightId WATER_STILL = new LightId("minecraft", "water_still");
    private static final LightId WATER = new LightId("minecraft", "water");

    static {
        registerColorProvider(
                (chunk, block, x, y, z) -> {
                    int color = getAverageBiomeColor(chunk.getPos(), BiomeColorType.Grass, x, z);

                    int average = BlockColors.get(GRASS_BLOCK_TOP);
                    if (average == 0) return color;

                    int r = (color) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = (color >> 16) & 0xFF;

                    int averageR = (average) & 0xFF;
                    int averageG = (average >> 8) & 0xFF;
                    int averageB = (average >> 16) & 0xFF;

                    r = r * averageR / 255;
                    g = g * averageG / 255;
                    b = b * averageB / 255;

                    return (255 << 24) | (b << 16) | (g << 8) | (r);
                },
                "grass_block"
        );

        registerColorProvider(
                (chunk, block, x, y, z) -> {
                    int color = getAverageBiomeColor(chunk.getPos(), BiomeColorType.Grass, x, z);

                    int average = BlockColors.get(GRASS);
                    if (average == 0) return color;

                    int r = (color) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = (color >> 16) & 0xFF;

                    int averageR = (average) & 0xFF;
                    int averageG = (average >> 8) & 0xFF;
                    int averageB = (average >> 16) & 0xFF;

                    r = r * averageR / 255;
                    g = g * averageG / 255;
                    b = b * averageB / 255;

                    return (255 << 24) | (b << 16) | (g << 8) | (r);
                },
                "fern",
                "grass",
                "tall_grass"
        );

        registerColorProvider(
                (chunk, block, x, y, z) -> {
                    int color = getAverageBiomeColor(chunk.getPos(), BiomeColorType.Foliage, x, z);

                    int average = BlockColors.get(block);
                    if (average == 0) return color;

                    int r = (color) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = (color >> 16) & 0xFF;

                    int averageR = (average) & 0xFF;
                    int averageG = (average >> 8) & 0xFF;
                    int averageB = (average >> 16) & 0xFF;

                    r = r * averageR / 255;
                    g = g * averageG / 255;
                    b = b * averageB / 255;

                    return (255 << 24) | (b << 16) | (g << 8) | (r);
                },
                "acacia_leaves",
                "birch_leaves",
                "dark_oak_leaves",
                "jungle_leaves",
                "mangrove_leaves",
                "oak_leaves",
                "spruce_leaves",
                "vine"
        );

        registerColorProvider(
                (chunk, block, x, y, z) -> {
                    int color = getAverageBiomeColor(chunk.getPos(), BiomeColorType.Water, x, z);

                    int average = BlockColors.get(WATER_STILL);
                    if (average == 0) return color;

                    int r = (color) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = (color >> 16) & 0xFF;

                    int averageR = (average) & 0xFF;
                    int averageG = (average >> 8) & 0xFF;
                    int averageB = (average >> 16) & 0xFF;

                    r = r * averageR / 255;
                    g = g * averageG / 255;
                    b = b * averageB / 255;

                    return (255 << 24) | (b << 16) | (g << 8) | (r);
                },
                "water",
                "bubble_column",
                "water_cauldron"
        );
    }

    private static void registerColorProvider(ColorProvider provider, String... ids) {
        for (String id : ids) {
            COLOR_PROVIDERS.put(new LightId("minecraft", id), provider);
        }
    }

    private interface ColorProvider {
        int get(LightChunk chunk, LightBlock block, int x, int y, int z);
    }

    private static int mix(int abgr1, int abgr2, float ratio) {
        if (ratio >= 1) return abgr2;
        else if (ratio <= 0) return abgr1;

        float iRatio = 1f - ratio;

        int r1 = (abgr1) & 0xFF;
        int g1 = (abgr1 >> 8) & 0xFF;
        int b1 = (abgr1 >> 16) & 0xFF;

        int r2 = (abgr2) & 0xFF;
        int g2 = (abgr2 >> 8) & 0xFF;
        int b2 = (abgr2 >> 16) & 0xFF;

        int r = (int) ((r1 * iRatio) + (r2 * ratio));
        int g = (int) ((g1 * iRatio) + (g2 * ratio));
        int b = (int) ((b1 * iRatio) + (b2 * ratio));

        return (0xFF << 24) | (b << 16) | (g << 8) | (r);
    }

    private static int toArgb(int abgr, Brightness brightness) {
        int r = (abgr) & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        int a = (abgr >> 24) & 0xFF;

        r = r * brightness.value / 255;
        g = g * brightness.value / 255;
        b = b * brightness.value / 255;

        return (a << 24) | (r << 16) | (g << 8) | (b);
    }
}
