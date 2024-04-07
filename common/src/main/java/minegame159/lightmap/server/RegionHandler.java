package minegame159.lightmap.server;

import minegame159.lightmap.LightMap;
import org.microhttp.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class RegionHandler {
    private Path folder;

    public Response get(Map<String, String> query) {
        int x, z;

        try {
            x = Integer.parseInt(query.get("x"));
            z = Integer.parseInt(query.get("z"));
        }
        catch (NumberFormatException ignored) {
            return HttpUtils.newStringResponse(400, "Invalid position");
        }

        if (folder == null) {
            folder = LightMap.get().getPlatform().getDataFolder().resolve("minecraft:overworld");
        }

        Path path = folder.resolve("r_" + x + "_" + z + ".png");

        if (Files.exists(path)) {
            try {
                byte[] bytes = Files.readAllBytes(path);
                return HttpUtils.newByteResponse(200, "image/png", bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return HttpUtils.newStringResponse(400, "Region not found");
    }
}
