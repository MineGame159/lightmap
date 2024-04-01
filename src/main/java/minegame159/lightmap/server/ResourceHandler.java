package minegame159.lightmap.server;

import org.microhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ResourceHandler {
    private final String path;
    private final Map<String, Resource> resources = new HashMap<>();

    public ResourceHandler(String path) {
        this.path = path;
    }

    public synchronized Response get(URI uri) {
        String path = this.path + uri.getPath();
        Resource resource = resources.get(path);

        if (resource == null) {
            InputStream in = ResourceHandler.class.getResourceAsStream(path);
            byte[] bytes = null;

            if (in != null) {
                bytes = read(in);
            }

            resource = new Resource(HttpUtils.getContentType(uri), bytes);
            resources.put(path, resource);
        }

        if (resource.bytes == null) {
            return HttpUtils.newStringResponse(404, "Not Found");
        }

        return HttpUtils.newByteResponse(200, resource.contentType, resource.bytes);
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    private static byte[] read(InputStream in) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            int read;
            byte[] buffer = new byte[4096];

            while ((read = in.read(buffer)) > 0) {
                bytes.write(buffer, 0, read);
            }

            return bytes.toByteArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record Resource(String contentType, byte[] bytes) {}
}
