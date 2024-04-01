package minegame159.lightmap.server;

import org.microhttp.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class RegionHandler {
    private final File directory;

    public RegionHandler(File directory) {
        this.directory = directory;
    }

    public Response get(Map<String, String> query) {
        int x, z;

        try {
            x = Integer.parseInt(query.get("x"));
            z = Integer.parseInt(query.get("z"));
        }
        catch (NumberFormatException ignored) {
            return HttpUtils.newStringResponse(400, "Invalid position");
        }

        File file = new File(directory, "r_" + x + "_" + z + ".png");

        if (file.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                return HttpUtils.newByteResponse(200, "image/png", bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return HttpUtils.newStringResponse(400, "Region not found");
    }
}
