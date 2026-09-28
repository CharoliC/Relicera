package com.yukari.relicera.common.network.packet;

import com.yukari.relicera.common.item.SelectableItemContents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record SelectItemContentsSlotPacket(int containerId, int slotId, int selectedSlot) {
    public static void encode(SelectItemContentsSlotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.containerId);
        buffer.writeVarInt(packet.slotId);
        buffer.writeVarInt(packet.selectedSlot);
    }

    public static SelectItemContentsSlotPacket decode(FriendlyByteBuf buffer) {
        return new SelectItemContentsSlotPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(SelectItemContentsSlotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && player.isAlive() && !player.isSpectator()) {
            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId == packet.containerId && menu.stillValid(player)
                    && packet.slotId >= 0 && packet.slotId < menu.slots.size()) {
                Slot slot = menu.getSlot(packet.slotId);
                ItemStack stack = slot.getItem();
                if (slot.isActive() && slot.allowModification(player) && stack.getCount() == 1
                        && stack.getItem() instanceof SelectableItemContents contents
                        && packet.selectedSlot >= 0 && packet.selectedSlot < contents.getContentsSlotCount()) {
                    contents.setSelectedContentsSlot(stack, packet.selectedSlot);
                    slot.setChanged();
                    menu.broadcastChanges();
                }
            }
        }
        context.setPacketHandled(true);
    }
}
