package net.akachi.auth;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AuthPacket(String accessToken) {
    public static void encode(AuthPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.accessToken(), 8192);
    }

    public static AuthPacket decode(FriendlyByteBuf buffer) {
        return new AuthPacket(buffer.readUtf(8192));
    }

    public static void handle(AuthPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                AuthService.verify(player, packet.accessToken());
            }
        });
        context.setPacketHandled(true);
    }
}
