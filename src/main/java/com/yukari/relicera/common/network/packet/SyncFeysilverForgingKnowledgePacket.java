package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.common.item.feysilver.FeysilverForgingClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncFeysilverForgingKnowledgePacket(boolean learnedVolumeOne) {
    public static void encode(SyncFeysilverForgingKnowledgePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.learnedVolumeOne);
    }

    public static SyncFeysilverForgingKnowledgePacket decode(FriendlyByteBuf buffer) {
        return new SyncFeysilverForgingKnowledgePacket(buffer.readBoolean());
    }

    public static void handle(SyncFeysilverForgingKnowledgePacket packet, Supplier<NetworkEvent.Context> context) {
        FeysilverForgingClientData.update(packet.learnedVolumeOne);
    }
}
