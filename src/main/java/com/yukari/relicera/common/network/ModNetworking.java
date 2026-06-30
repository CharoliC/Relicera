package com.yukari.relicera.common.network;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.network.packet.SyncAstralObservationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static int messageId;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.messageBuilder(SyncAstralObservationPacket.class, nextMessageId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncAstralObservationPacket::encode)
                .decoder(SyncAstralObservationPacket::decode)
                .consumerMainThread(SyncAstralObservationPacket::handle)
                .add();
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    private static int nextMessageId() {
        return messageId++;
    }
}
