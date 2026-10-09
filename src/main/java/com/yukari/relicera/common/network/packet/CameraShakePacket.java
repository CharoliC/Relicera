package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.client.effect.CameraShakeEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CameraShakePacket(int durationTicks, float intensity) {
    public static void encode(CameraShakePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.durationTicks());
        buffer.writeFloat(packet.intensity());
    }

    public static CameraShakePacket decode(FriendlyByteBuf buffer) {
        return new CameraShakePacket(buffer.readVarInt(), buffer.readFloat());
    }

    public static void handle(
            CameraShakePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                CameraShakeEffect.start(packet.durationTicks(), packet.intensity()));
        contextSupplier.get().setPacketHandled(true);
    }
}
