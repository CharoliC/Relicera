package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.common.curio.RippleheartRingClientData;
import com.yukari.relicera.common.curio.RippleheartRingEffects.ActiveState;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public record SyncRippleheartRingStatePacket(
        ActiveState activeState,
        Optional<UUID> pairId,
        Component redWearerName,
        Component blueWearerName
) {
    public static void encode(SyncRippleheartRingStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.activeState);
        buffer.writeBoolean(packet.pairId.isPresent());
        packet.pairId.ifPresent(buffer::writeUUID);
        buffer.writeComponent(packet.redWearerName);
        buffer.writeComponent(packet.blueWearerName);
    }

    public static SyncRippleheartRingStatePacket decode(FriendlyByteBuf buffer) {
        ActiveState activeState = buffer.readEnum(ActiveState.class);
        Optional<UUID> pairId = buffer.readBoolean() ? Optional.of(buffer.readUUID()) : Optional.empty();
        Component redWearerName = buffer.readComponent();
        Component blueWearerName = buffer.readComponent();
        return new SyncRippleheartRingStatePacket(activeState, pairId, redWearerName, blueWearerName);
    }

    public static void handle(SyncRippleheartRingStatePacket packet, Supplier<NetworkEvent.Context> context) {
        RippleheartRingClientData.update(
                packet.activeState,
                packet.pairId,
                packet.redWearerName,
                packet.blueWearerName
        );
    }
}
