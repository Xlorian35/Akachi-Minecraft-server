package net.akachi.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

final class AuthService {
    private static final String SUPABASE_URL = "https://cbjtejlgkymaoedvuong.supabase.co";
    // Public publishable key only; never use a Supabase secret/service_role key in a client mod.
    private static final String SUPABASE_PUBLISHABLE_KEY = "sb_publishable_uLCNckMO7E3-N6JFYEmZ_A_toGH390_";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, Long> AUTHENTICATED_UNTIL = new ConcurrentHashMap<>();

    private AuthService() {}

    static void requireAuthentication(ServerPlayer player) {
        UUID id = player.getUUID();
        AUTHENTICATED_UNTIL.remove(id);
        PENDING.add(id);
        MinecraftServer server = player.getServer();
        if (server != null) {
            CompletableFuture.delayedExecutor(20, java.util.concurrent.TimeUnit.SECONDS).execute(() -> {
                if (PENDING.remove(id)) {
                    server.execute(() -> {
                        if (player.isAlive()) disconnect(player,
                                "Akachi hesabı doğrulanmadı. Akachi Launcher'dan giriş yapıp tekrar bağlan.");
                    });
                }
            });
        }
    }

    static void verify(ServerPlayer player, String token) {
        UUID playerId = player.getUUID();
        if (!PENDING.contains(playerId)) return;

        if (token == null || token.isBlank() || token.length() > 8192) {
            if (PENDING.remove(playerId)) {
                disconnect(player, "Akachi Launcher oturumu bulunamadı. Oyunu Akachi Launcher üzerinden başlat.");
            }
            return;
        }
        long expiresAt = tokenExpiryEpochSeconds(token);
        if (expiresAt <= Instant.now().getEpochSecond()) {
            if (PENDING.remove(playerId)) {
                disconnect(player, "Akachi oturumunun süresi dolmuş. Akachi Launcher'dan tekrar giriş yap.");
            }
            return;
        }
        CompletableFuture.runAsync(() -> {
            boolean accepted = false;
            try {
                JsonObject user = getJson("/auth/v1/user", token);
                String userId = string(user, "id");
                if (!userId.isBlank()) {
                    String endpoint = "/rest/v1/akachi_profiles?select=minecraft_username&user_id=eq." + userId;
                    JsonArray rows = JsonParser.parseString(get(endpoint, token)).getAsJsonArray();
                    if (!rows.isEmpty()) {
                        String registeredName = string(rows.get(0).getAsJsonObject(), "minecraft_username");
                        accepted = registeredName.equalsIgnoreCase(player.getGameProfile().getName());
                    }
                }
            } catch (Exception ignored) {
                // Do not expose access tokens, HTTP responses, or account details in server logs.
            }

            boolean result = accepted;
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.execute(() -> {
                    if (!PENDING.remove(player.getUUID())) return;
                    if (!result) {
                        disconnect(player, "Akachi hesabı bu Minecraft kullanıcı adına bağlı değil veya oturum geçersiz.");
                    } else {
                        AUTHENTICATED_UNTIL.put(player.getUUID(), expiresAt);
                        scheduleExpiryKick(player, expiresAt);
                    }
                });
            }
        });
    }

    private static long tokenExpiryEpochSeconds(String token) {
        try {
            String[] segments = token.split("\\.");
            if (segments.length != 3) return 0L;
            byte[] payload = java.util.Base64.getUrlDecoder().decode(segments[1]);
            JsonObject claims = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonElement expiry = claims.get("exp");
            return expiry == null || expiry.isJsonNull() ? 0L : expiry.getAsLong();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static void scheduleExpiryKick(ServerPlayer player, long expiresAt) {
        UUID playerId = player.getUUID();
        long delayMillis = Math.max(0L, expiresAt * 1000L - System.currentTimeMillis());
        CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS).execute(() -> {
            MinecraftServer server = player.getServer();
            if (server == null) return;
            server.execute(() -> {
                Long activeExpiry = AUTHENTICATED_UNTIL.get(playerId);
                if (activeExpiry != null && activeExpiry == expiresAt
                        && server.getPlayerList().getPlayer(playerId) == player) {
                    AUTHENTICATED_UNTIL.remove(playerId, activeExpiry);
                    disconnect(player, "Akachi oturumunun süresi doldu. Tekrar giriş yapıp oyuna yeniden bağlan.");
                }
            });
        });
    }

    private static JsonObject getJson(String endpoint, String token) throws Exception {
        return JsonParser.parseString(get(endpoint, token)).getAsJsonObject();
    }

    private static String get(String endpoint, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(SUPABASE_URL + endpoint))
                .timeout(Duration.ofSeconds(12))
                .header("apikey", SUPABASE_PUBLISHABLE_KEY)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Supabase rejected the session");
        }
        return response.body();
    }

    private static String string(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static void disconnect(ServerPlayer player, String message) {
        if (player.connection != null) player.connection.disconnect(Component.literal(message));
    }
}
