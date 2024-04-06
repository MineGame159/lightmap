package minegame159.lightmap.server;

import org.microhttp.EventLoop;
import org.microhttp.Options;
import org.microhttp.Request;
import org.microhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

public class LightServer {
    private final RegionHandler region;
    private final ClaimsHandler claims;
    private final ResourceHandler ui;

    private final EventLoop eventLoop;

    public LightServer(File directory) {
        this.region = new RegionHandler(directory);
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

        Response response = switch (uri.getPath()) {
            case "/api/region" -> region.get(HttpUtils.parseQuery(uri));
            case "/api/claims" -> claims.get();

            case "/" -> ui.get(URI.create("/index.html"));
            default -> ui.get(uri);
        };

        callback.accept(response);
    }
}
