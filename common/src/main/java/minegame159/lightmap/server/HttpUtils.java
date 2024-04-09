package minegame159.lightmap.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.microhttp.Header;
import org.microhttp.Response;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtils {
    private static final Gson GSON = new GsonBuilder().create();

    public static Response newJsonResponse(int status, Object body) {
        return newByteResponse(status, "application/json; charset=utf-8", GSON.toJson(body).getBytes(StandardCharsets.UTF_8));
    }

    public static Response newStringResponse(int status, String body) {
        return newByteResponse(status, "text/plain; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }

    public static Response newByteResponse(int status, String contentType, byte[] body) {
        String reason = switch (status) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            default -> "Error";
        };

        return new Response(
                status,
                reason,
                List.of(
                        new Header("Content-Type", contentType),
                        new Header("Content-Length", Integer.toString(body.length)),
                        new Header("Access-Control-Allow-Origin", "*")
                ),
                body
        );
    }

    public static String getContentType(URI uri) {
        String extension = uri.getPath().substring(uri.getPath().lastIndexOf('.') + 1);

        return switch (extension) {
            case "html" -> "text/html; charset=utf-8";
            case "css" -> "text/css; charset=utf-8";
            case "js" -> "text/javascript; charset=utf-8";
            default -> "text/plain; charset=utf-8";
        };
    }

    public static Map<String, String> parseQuery(URI uri) {
        if (uri.getQuery() == null) return Map.of();

        Map<String, String> query = new HashMap<>();
        String[] pairs = uri.getQuery().split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query.put(pair.substring(0, idx), pair.substring(idx + 1));
        }

        return query;
    }
}
