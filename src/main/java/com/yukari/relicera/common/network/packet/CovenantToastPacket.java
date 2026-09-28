package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.client.toast.CovenantToast;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record CovenantToastPacket(Kind kind, ResourceLocation entityTypeId) {
    public enum Kind {
        ESTABLISHED,
        BROKEN
    }

    public static void encode(CovenantToastPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.kind());
        buffer.writeResourceLocation(packet.entityTypeId());
    }

    public static CovenantToastPacket decode(FriendlyByteBuf buffer) {
        return new CovenantToastPacket(buffer.readEnum(Kind.class), buffer.readResourceLocation());
    }

    public static void handle(CovenantToastPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                CovenantToast.show(packet.kind(), packet.entityTypeId()));
        contextSupplier.get().setPacketHandled(true);
    }
}
