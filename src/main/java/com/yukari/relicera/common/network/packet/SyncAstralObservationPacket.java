package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.common.astral.AstralObservationClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncAstralObservationPacket(int observedMoonPhaseMask, boolean claimedAstralLens) {
    public static void encode(SyncAstralObservationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.observedMoonPhaseMask);
        buffer.writeBoolean(packet.claimedAstralLens);
    }

    public static SyncAstralObservationPacket decode(FriendlyByteBuf buffer) {
        return new SyncAstralObservationPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(SyncAstralObservationPacket packet, Supplier<NetworkEvent.Context> context) {
        AstralObservationClientData.update(packet.observedMoonPhaseMask, packet.claimedAstralLens);
    }
}
