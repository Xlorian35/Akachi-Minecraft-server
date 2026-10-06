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
import java.time.Duration;
import java.util.Set;
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

    private AuthService() {}

    static void requireAuthentication(ServerPlayer player) {
        UUID id = player.getUUID();
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
        if (token == null || token.isBlank() || token.length() > 8192 || !PENDING.contains(player.getUUID())) {
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
                    }
                });
            }
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
