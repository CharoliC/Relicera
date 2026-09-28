package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.client.effect.ThousandweightGauntletsSmashEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ThousandweightGauntletsSmashPacket(
        double x,
        double y,
        double z,
        int blockStateId,
        boolean windBurst
) {
    public static void encode(ThousandweightGauntletsSmashPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x());
        buffer.writeDouble(packet.y());
        buffer.writeDouble(packet.z());
        buffer.writeVarInt(packet.blockStateId());
        buffer.writeBoolean(packet.windBurst());
    }

    public static ThousandweightGauntletsSmashPacket decode(FriendlyByteBuf buffer) {
        return new ThousandweightGauntletsSmashPacket(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt(),
                buffer.readBoolean()
        );
    }

    public static void handle(
            ThousandweightGauntletsSmashPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ThousandweightGauntletsSmashEffect.triggerSmash(
                        packet.x(),
                        packet.y(),
                        packet.z(),
                        packet.blockStateId(),
                        packet.windBurst()
                ));
        contextSupplier.get().setPacketHandled(true);
    }
}
