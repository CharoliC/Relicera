package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.client.effect.BruteBeltClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record SyncAttackStrengthPacket(int attackStrengthTicker) {
    public static void encode(SyncAttackStrengthPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.attackStrengthTicker());
    }

    public static SyncAttackStrengthPacket decode(FriendlyByteBuf buffer) {
        return new SyncAttackStrengthPacket(Math.max(0, buffer.readVarInt()));
    }

    public static void handle(SyncAttackStrengthPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                BruteBeltClientEffects.syncAttackStrength(packet.attackStrengthTicker())));
        context.setPacketHandled(true);
    }
}
