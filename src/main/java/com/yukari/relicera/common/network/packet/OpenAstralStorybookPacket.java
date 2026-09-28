package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.client.screen.AstralStorybookScreen;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record OpenAstralStorybookPacket(ItemStack storybook) {
    public static void encode(OpenAstralStorybookPacket packet, FriendlyByteBuf buffer) {
        buffer.writeItem(packet.storybook);
    }

    public static OpenAstralStorybookPacket decode(FriendlyByteBuf buffer) {
        return new OpenAstralStorybookPacket(buffer.readItem());
    }

    public static void handle(OpenAstralStorybookPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AstralStorybookScreen.open(packet.storybook));
    }
}
