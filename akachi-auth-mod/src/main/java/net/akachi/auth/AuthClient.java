package net.akachi.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = AkachiAuth.MOD_ID, value = Dist.CLIENT)
public final class AuthClient {
    private AuthClient() {}

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        String token = "";
        try {
            Path authFile = FMLPaths.GAMEDIR.get().resolve("akachi-auth.json");
            if (Files.isRegularFile(authFile)) {
                JsonObject auth = JsonParser.parseString(Files.readString(authFile, StandardCharsets.UTF_8)).getAsJsonObject();
                token = auth.has("access_token") ? auth.get("access_token").getAsString() : "";
            }
        } catch (Exception ignored) {
            // Send an empty token so the server rejects this connection immediately.
        }
        AkachiAuth.CHANNEL.sendToServer(new AuthPacket(token));
    }
}
