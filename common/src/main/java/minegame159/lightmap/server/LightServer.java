package minegame159.lightmap.server;

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

        if (uri.getPath().startsWith("/api/")) {
            callback.accept(routeApi(uri));
            return;
        }

        Response response = switch (uri.getPath()) {
            case "/" -> ui.get(URI.create("/index.html"));

            default -> ui.get(uri);
        };

        callback.accept(response);
    }

    private Response routeApi(URI uri) {
        int slashI = uri.getPath().indexOf('/', 5);
        if (slashI == -1) return HttpUtils.newStringResponse(404, "Not Found");

        LightId world = LightId.of(uri.getPath().substring(5, slashI));
        String after = uri.getPath().substring(slashI);

        return switch (after) {
            case "/region" -> region.get(world, HttpUtils.parseQuery(uri));
            case "/claims" -> claims.get(world);

            default -> HttpUtils.newStringResponse(404, "Not Found");
        };
    }
}
