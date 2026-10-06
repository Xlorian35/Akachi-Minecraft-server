package net.akachi.auth;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(AkachiAuth.MOD_ID)
public final class AkachiAuth {
    public static final String MOD_ID = "akachi_auth";
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);

    public AkachiAuth() {
        CHANNEL.messageBuilder(AuthPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AuthPacket::encode)
                .decoder(AuthPacket::decode)
                .consumerMainThread(AuthPacket::handle)
                .add();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void playerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                AuthService.requireAuthentication(player);
            }
        }
    }
}
