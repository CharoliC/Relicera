package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.common.item.TurncoatsMedalItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UseTurncoatsMedalBannerPacket() {
    public static void encode(UseTurncoatsMedalBannerPacket packet, FriendlyByteBuf buffer) {
    }

    public static UseTurncoatsMedalBannerPacket decode(FriendlyByteBuf buffer) {
        return new UseTurncoatsMedalBannerPacket();
    }

    public static void handle(
            UseTurncoatsMedalBannerPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            TurncoatsMedalItem.handleCreativeBannerInteraction(sender);
        }
        context.setPacketHandled(true);
    }
}
