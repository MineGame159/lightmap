package minegame159.lightmap.resources;

import com.google.gson.Gson;
import minegame159.lightmap.LightMap;
import minegame159.lightmap.platform.LightPlatform;
import minegame159.lightmap.utils.LightId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceManager {
    private final LightPlatform platform;
    private final File clientFile;

    private JarFile clientJar;

    public ResourceManager(LightPlatform platform) {
        platform.getCacheFolder().toFile().mkdirs();

        this.platform = platform;
        this.clientFile = platform.getCacheFolder().resolve("client-" + platform.getMcVersion() + ".jar").toFile();
    }

    public BufferedImage getTexture(LightId id) {
        InputStream in = get("textures/block", id);
        if (in == null) return null;

        try {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream get(String folder, LightId id) {
        // Vanilla
        if (id.namespace().equals("minecraft")) {
            if (clientJar == null) openJar();

            JarEntry entry = clientJar.getJarEntry("assets/minecraft/" + folder + "/" + id.path() + ".png");
            if (entry == null) return null;

            try {
                return clientJar.getInputStream(entry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Modded
        //return ResourceManager.class.getResourceAsStream("/assets/" + id.namespace() + "/" + folder + "/" + id.path() + ".png");
        return null;
    }

    private void openJar() {
        if (!clientFile.exists()) downloadJar();

        try {
            clientJar = new JarFile(clientFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadJar() {
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = new Gson();

        VersionManifest manifest;

        try {
            InputStream in = client.send(HttpRequest.newBuilder(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest.json")).build(), HttpResponse.BodyHandlers.ofInputStream()).body();
            manifest = gson.fromJson(new InputStreamReader(in), VersionManifest.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Version version : manifest.versions) {
            if (version.id.equals(platform.getMcVersion())) {
                VersionMeta meta;

                try {
                    InputStream in = client.send(HttpRequest.newBuilder(URI.create(version.url)).build(), HttpResponse.BodyHandlers.ofInputStream()).body();
                    meta = gson.fromJson(new InputStreamReader(in), VersionMeta.class);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    client.send(HttpRequest.newBuilder(URI.create(meta.downloads.client.url)).build(), HttpResponse.BodyHandlers.ofFile(clientFile.toPath()));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                break;
            }
        }

        if (!clientFile.exists()) {
            LightMap.LOG.error("Failed to download client jar");
        }
    }

    public void close() {
        if (clientJar != null) {
            try {
                clientJar.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            clientJar = null;
        }
    }
}
