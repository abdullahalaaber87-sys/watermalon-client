package dev.watermalon.spotify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.sun.net.httpserver.HttpServer;
import dev.watermalon.Watermalon;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * Spotify Web API client using Authorization Code with PKCE (no client secret).
 * All network work runs on a single background thread; textures are created on the client thread.
 */
public final class Spotify {
    private Spotify() {}

    public static final String REDIRECT = "http://127.0.0.1:43821/callback";
    private static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-currently-playing";
    public static final Identifier ART = Identifier.fromNamespaceAndPath("watermalon", "spotify_art");
    private static final int ART_SIZE = 64;

    public record Playback(String title, String artist, String artUrl, long progressMs, long durationMs, boolean playing, long sampledNanos) {
        public static final Playback EMPTY = new Playback("", "", "", 0, 0, false, 0);

        public boolean active() { return !title.isEmpty(); }

        public long positionNow() {
            if (durationMs <= 0) return 0;
            long elapsed = playing ? (System.nanoTime() - sampledNanos) / 1_000_000L : 0;
            return Math.min(durationMs, progressMs + Math.max(0, elapsed));
        }
    }

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "watermalon-spotify");
        thread.setDaemon(true);
        return thread;
    });
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final AtomicBoolean BUSY = new AtomicBoolean();

    private static volatile Playback playback = Playback.EMPTY;
    private static volatile String status = "Not connected";
    private static volatile boolean authorizing;
    private static volatile boolean artReady;
    private static String clientId = "";
    private static String accessToken = "";
    private static String refreshToken = "";
    private static long expiresAt;
    private static long retryAfter;
    private static String loadedArt = "";

    public static Playback playback() { return playback; }
    public static boolean connected() { return !refreshToken.isEmpty(); }
    public static boolean artReady() { return artReady && playback.active(); }
    public static String status() { return status; }

    public static String statusLabel() {
        if (authorizing) return "Waiting...";
        return connected() ? "Disconnect" : "Connect";
    }

    // Config persistence. Only the refresh token and client id are stored.
    public static void restore(String id, String refresh) {
        clientId = id == null ? "" : id;
        refreshToken = refresh == null ? "" : refresh;
        if (connected()) status = "Connected";
    }

    public static String savedRefreshToken() { return refreshToken; }
    public static String savedClientId() { return clientId; }

    public static void connect(String id) {
        if (connected()) {
            disconnect();
            return;
        }
        if (authorizing) return;
        String trimmed = id == null ? "" : id.trim();
        if (!trimmed.matches("[A-Za-z0-9]{16,64}")) {
            status = "Paste your Client ID first";
            return;
        }
        clientId = trimmed;
        authorizing = true;
        status = "Check your browser";
        WORKER.execute(Spotify::authorize);
    }

    public static void disconnect() {
        accessToken = "";
        refreshToken = "";
        expiresAt = 0;
        playback = Playback.EMPTY;
        status = "Not connected";
        Minecraft.getInstance().execute(Spotify::releaseArt);
        Watermalon.saveConfig();
    }

    private static void authorize() {
        HttpServer server = null;
        try {
            byte[] random = new byte[64];
            new SecureRandom().nextBytes(random);
            String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
            byte[] stateBytes = new byte[16];
            new SecureRandom().nextBytes(stateBytes);
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);
            CompletableFuture<String> code = new CompletableFuture<>();

            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 43821), 0);
            server.createContext("/callback", exchange -> {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
                boolean ok = state.equals(query.get("state")) && query.containsKey("code");
                byte[] body = (ok ? "Watermalon is connected to Spotify. You can close this tab." : "Spotify login failed. You can close this tab.").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(ok ? 200 : 400, body.length);
                try (OutputStream out = exchange.getResponseBody()) { out.write(body); }
                if (ok) code.complete(query.get("code"));
                else code.completeExceptionally(new IllegalStateException(query.getOrDefault("error", "denied")));
            });
            server.start();

            String url = "https://accounts.spotify.com/authorize?response_type=code"
                    + "&client_id=" + enc(clientId)
                    + "&redirect_uri=" + enc(REDIRECT)
                    + "&scope=" + enc(SCOPES)
                    + "&state=" + enc(state)
                    + "&code_challenge_method=S256&code_challenge=" + enc(challenge);
            Minecraft.getInstance().execute(() -> Util.getPlatform().openUri(URI.create(url)));

            String authCode = code.get(120, TimeUnit.SECONDS);
            JsonObject token = tokenRequest("grant_type=authorization_code&code=" + enc(authCode)
                    + "&redirect_uri=" + enc(REDIRECT) + "&client_id=" + enc(clientId) + "&code_verifier=" + enc(verifier));
            if (token == null) throw new IllegalStateException("token");
            applyToken(token);
            status = "Connected";
            Watermalon.saveConfig();
            refreshPlayback();
        } catch (Exception failure) {
            status = "Login failed. Check the redirect URI";
            Watermalon.LOGGER.warn("Spotify login failed: {}", failure.toString());
        } finally {
            authorizing = false;
            if (server != null) server.stop(0);
        }
    }

    /** Schedules a playback refresh if one is not already running. */
    public static void poll() {
        if (!connected() || authorizing || System.currentTimeMillis() < retryAfter || !BUSY.compareAndSet(false, true)) return;
        WORKER.execute(() -> {
            try {
                refreshPlayback();
            } finally {
                BUSY.set(false);
            }
        });
    }

    public static void command(String action) {
        if (!connected()) return;
        WORKER.execute(() -> {
            String method = "POST";
            String path = switch (action) {
                case "next" -> "/me/player/next";
                case "previous" -> "/me/player/previous";
                default -> {
                    method = "PUT";
                    yield playback.playing() ? "/me/player/pause" : "/me/player/play";
                }
            };
            HttpResponse<String> response = api(method, path);
            if (response != null && response.statusCode() == 403) status = "Controls need Spotify Premium";
            sleepQuietly(350);
            refreshPlayback();
        });
    }

    private static void refreshPlayback() {
        HttpResponse<String> response = api("GET", "/me/player?additional_types=episode");
        if (response == null) return;
        int code = response.statusCode();
        if (code == 204) {
            playback = Playback.EMPTY;
            return;
        }
        if (code == 429) {
            long seconds = response.headers().firstValueAsLong("Retry-After").orElse(10);
            retryAfter = System.currentTimeMillis() + seconds * 1000L;
            return;
        }
        if (code != 200) return;
        try {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject item = obj(json, "item");
            if (item == null) {
                playback = Playback.EMPTY;
                return;
            }
            String title = str(item, "name");
            StringBuilder artists = new StringBuilder();
            JsonArray artistArray = arr(item, "artists");
            if (artistArray != null) {
                for (JsonElement artist : artistArray) {
                    if (!artist.isJsonObject()) continue;
                    if (!artists.isEmpty()) artists.append(", ");
                    artists.append(str(artist.getAsJsonObject(), "name"));
                }
            } else {
                JsonObject show = obj(item, "show");
                if (show != null) artists.append(str(show, "name"));
            }
            JsonObject album = obj(item, "album");
            JsonArray images = arr(album != null ? album : item, "images");
            String art = "";
            if (images != null && !images.isEmpty()) {
                JsonElement smallest = images.get(images.size() - 1);
                if (smallest.isJsonObject()) art = str(smallest.getAsJsonObject(), "url");
            }
            long progress = json.has("progress_ms") && !json.get("progress_ms").isJsonNull() ? json.get("progress_ms").getAsLong() : 0;
            long duration = item.has("duration_ms") ? item.get("duration_ms").getAsLong() : 0;
            boolean playing = json.has("is_playing") && json.get("is_playing").getAsBoolean();
            playback = new Playback(title, artists.toString(), art, progress, duration, playing, System.nanoTime());
            if (!art.isEmpty() && !art.equals(loadedArt)) loadArt(art);
        } catch (RuntimeException malformed) {
            Watermalon.LOGGER.debug("Unreadable Spotify response", malformed);
        }
    }

    private static HttpResponse<String> api(String method, String path) {
        if (!ensureToken()) return null;
        HttpResponse<String> response = send(method, path);
        if (response != null && response.statusCode() == 401) {
            expiresAt = 0;
            if (!ensureToken()) return null;
            response = send(method, path);
        }
        return response;
    }

    private static HttpResponse<String> send(String method, String path) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("https://api.spotify.com/v1" + path))
                    .timeout(Duration.ofSeconds(8))
                    .header("Authorization", "Bearer " + accessToken);
            if (method.equals("GET")) builder.GET();
            else builder.method(method, HttpRequest.BodyPublishers.noBody());
            return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception failure) {
            return null;
        }
    }

    private static boolean ensureToken() {
        if (!accessToken.isEmpty() && System.currentTimeMillis() < expiresAt - 30_000) return true;
        if (refreshToken.isEmpty() || clientId.isEmpty()) return false;
        JsonObject token = tokenRequest("grant_type=refresh_token&refresh_token=" + enc(refreshToken) + "&client_id=" + enc(clientId));
        if (token == null) return false;
        applyToken(token);
        Watermalon.saveConfig();
        return true;
    }

    private static JsonObject tokenRequest(String form) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://accounts.spotify.com/api/token"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 400 && response.body().contains("invalid_grant")) {
                refreshToken = "";
                status = "Session expired. Connect again";
                return null;
            }
            if (response.statusCode() != 200) return null;
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception failure) {
            return null;
        }
    }

    private static void applyToken(JsonObject token) {
        accessToken = str(token, "access_token");
        long lifetime = token.has("expires_in") ? token.get("expires_in").getAsLong() : 3600;
        expiresAt = System.currentTimeMillis() + lifetime * 1000L;
        String refresh = str(token, "refresh_token");
        if (!refresh.isEmpty()) refreshToken = refresh;
    }

    private static void loadArt(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (!"https".equals(uri.getScheme()) || host == null || !(host.endsWith(".scdn.co") || host.endsWith(".spotifycdn.com"))) return;
            HttpResponse<byte[]> response = HTTP.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200 || response.body().length > 2_000_000) return;
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(response.body()));
            if (source == null) return;
            BufferedImage scaled = new BufferedImage(ART_SIZE, ART_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, ART_SIZE, ART_SIZE, null);
            graphics.dispose();
            int[] pixels = scaled.getRGB(0, 0, ART_SIZE, ART_SIZE, null, 0, ART_SIZE);
            Minecraft.getInstance().execute(() -> {
                if (!url.equals(playback.artUrl())) return;
                NativeImage image = new NativeImage(ART_SIZE, ART_SIZE, false);
                for (int y = 0; y < ART_SIZE; y++) {
                    for (int x = 0; x < ART_SIZE; x++) image.setPixel(x, y, pixels[y * ART_SIZE + x] | 0xFF000000);
                }
                Minecraft.getInstance().getTextureManager().register(ART, new DynamicTexture(() -> "watermalon spotify art", image));
                loadedArt = url;
                artReady = true;
            });
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (Exception failure) {
            Watermalon.LOGGER.debug("Could not load Spotify artwork", failure);
        }
    }

    private static void releaseArt() {
        if (artReady) Minecraft.getInstance().getTextureManager().release(ART);
        artReady = false;
        loadedArt = "";
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null) return result;
        for (String pair : raw.split("&")) {
            int split = pair.indexOf('=');
            if (split <= 0) continue;
            result.put(URLDecoder.decode(pair.substring(0, split), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(split + 1), StandardCharsets.UTF_8));
        }
        return result;
    }

    private static String enc(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private static JsonObject obj(JsonObject parent, String key) {
        return parent != null && parent.has(key) && parent.get(key).isJsonObject() ? parent.getAsJsonObject(key) : null;
    }

    private static JsonArray arr(JsonObject parent, String key) {
        return parent != null && parent.has(key) && parent.get(key).isJsonArray() ? parent.getAsJsonArray(key) : null;
    }

    private static String str(JsonObject parent, String key) {
        return parent != null && parent.has(key) && parent.get(key).isJsonPrimitive() ? parent.get(key).getAsString() : "";
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
