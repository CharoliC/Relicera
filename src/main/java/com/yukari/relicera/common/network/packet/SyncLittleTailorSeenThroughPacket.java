package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.common.curio.LittleTailorsBeltClientData;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record SyncLittleTailorSeenThroughPacket(UUID entityId) {
    public static void encode(SyncLittleTailorSeenThroughPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.entityId);
    }

    public static SyncLittleTailorSeenThroughPacket decode(FriendlyByteBuf buffer) {
        return new SyncLittleTailorSeenThroughPacket(buffer.readUUID());
    }

    public static void handle(SyncLittleTailorSeenThroughPacket packet, Supplier<NetworkEvent.Context> context) {
        LittleTailorsBeltClientData.markSeenThrough(packet.entityId);
    }
}
