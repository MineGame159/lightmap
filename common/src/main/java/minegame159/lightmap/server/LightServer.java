package minegame159.lightmap.server;

import minegame159.lightmap.LightMap;
import minegame159.lightmap.utils.LightId;
import org.microhttp.EventLoop;
import org.microhttp.Options;
import org.microhttp.Request;
import org.microhttp.Response;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

public class LightServer {
    private final RegionHandler region;
    private final ClaimsHandler claims;
    private final ResourceHandler ui;

    private final EventLoop eventLoop;

    public LightServer() {
        this.region = new RegionHandler();
        this.claims = new ClaimsHandler();
        this.ui = new ResourceHandler("/web");

        Options options = Options.builder()
                .withHost("0.0.0.0")
                .withPort(8080)
                .withConcurrency(1)
                .build();

        try {
            eventLoop = new EventLoop(options, this::onRequest);
            eventLoop.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        eventLoop.stop();

        try {
            eventLoop.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void onRequest(Request req, Consumer<Response> callback) {
        URI uri = URI.create(req.uri());

        // API
        if (uri.getPath().startsWith("/api/")) {
            callback.accept(routeApi(uri));
            return;
        }

        // Other
        if (uri.getPath().equals("/")) {
            uri = URI.create("/index.html");
        }

        callback.accept(ui.get(uri));
    }

    private Response routeApi(URI uri) {
        String path = uri.getPath();

        // Global
        if (path.equals("/api/worlds")) {
            return HttpUtils.newJsonResponse(200, LightMap.get().getWorldIds());
        }

        // World specific
        int slashI = path.indexOf('/', 5);
        if (slashI == -1) return HttpUtils.newStringResponse(404, "Not Found");

        LightId worldId = LightId.of(path.substring(5, slashI));
        String after = path.substring(slashI);

        return switch (after) {
            case "/region" -> region.get(worldId, HttpUtils.parseQuery(uri));
            case "/claims" -> claims.get(worldId);

            default -> HttpUtils.newStringResponse(404, "Not Found");
        };
    }
}
