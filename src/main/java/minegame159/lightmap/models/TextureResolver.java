package minegame159.lightmap.models;

import com.google.gson.Gson;
import minegame159.lightmap.LightMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;

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
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TextureResolver {
    private final File clientFile;
    private JarFile clientJar;

    public TextureResolver() {
        Path cacheDir = FabricLoader.getInstance().getGameDir().resolve("cache");
        cacheDir.toFile().mkdirs();

        clientFile = cacheDir.resolve("client-" + SharedConstants.getGameVersion().getName() + ".jar").toFile();
    }

    public BufferedImage get(Identifier id) {
        if (clientJar == null) openJar();

        JarEntry entry = clientJar.getJarEntry("assets/minecraft/textures/block/" + id.getPath() + ".png");
        if (entry == null) return null;

        try {
            InputStream in = clientJar.getInputStream(entry);
            if (in == null) return null;

            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            if (version.id.equals(SharedConstants.getGameVersion().getName())) {
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
