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
        try {
            Path authFile = FMLPaths.GAMEDIR.get().resolve("akachi-auth.json");
            if (!Files.isRegularFile(authFile)) return;
            JsonObject auth = JsonParser.parseString(Files.readString(authFile, StandardCharsets.UTF_8)).getAsJsonObject();
            String token = auth.has("access_token") ? auth.get("access_token").getAsString() : "";
            if (!token.isBlank()) AkachiAuth.CHANNEL.sendToServer(new AuthPacket(token));
        } catch (Exception ignored) {
            // The server will time out and show the regular Akachi sign-in message.
        }
    }
}
