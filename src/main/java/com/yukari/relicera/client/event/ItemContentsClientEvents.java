package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.tooltip.ItemContentsCursorAnimation;
import com.yukari.relicera.common.item.SelectableItemContents;
import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.SelectItemContentsSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class ItemContentsClientEvents {
    private ItemContentsClientEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenRendered(ScreenEvent.Render.Post event) {
        ItemContentsCursorAnimation.finishFrame(event.getScreen());
    }

    @SubscribeEvent
    public static void onScreenClosed(ScreenEvent.Closing event) {
        ItemContentsCursorAnimation.close(event.getScreen());
    }

    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive() || player.isSpectator() || event.getScrollDelta() == 0.0D
                || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        Slot hovered = screen.getSlotUnderMouse();
        if (hovered == null || !hovered.isActive() || !hovered.allowModification(player)) {
            return;
        }
        ItemStack stack = hovered.getItem();
        if (stack.getCount() != 1 || !(stack.getItem() instanceof SelectableItemContents contents)) {
            return;
        }

        Slot target = hovered;
        boolean creativeInventory = screen instanceof CreativeModeInventoryScreen;
        AbstractContainerMenu targetMenu = creativeInventory ? player.inventoryMenu : player.containerMenu;
        if (creativeInventory) {
            if (hovered.container != player.getInventory()) {
                return;
            }
            target = player.inventoryMenu.slots.stream()
                    .filter(slot -> slot.container == player.getInventory() && slot.getItem() == stack)
                    .findFirst().orElse(null);
        } else if (screen.getMenu() != player.containerMenu) {
            return;
        }
        if (target == null || target.index < 0 || target.index >= targetMenu.slots.size()
                || targetMenu.getSlot(target.index) != target) {
            return;
        }

        int direction = event.getScrollDelta() > 0.0D ? -1 : 1;
        int previous = contents.getSelectedContentsSlot(stack);
        int selected = Math.floorMod(previous + direction, contents.getContentsSlotCount());
        ItemContentsCursorAnimation.select(screen, hovered, previous, selected, contents.getContentsSlotCount());
        contents.setSelectedContentsSlot(stack, selected);
        if (creativeInventory) {
            targetMenu.broadcastChanges();
        } else {
            ModNetworking.sendToServer(new SelectItemContentsSlotPacket(targetMenu.containerId, target.index, selected));
        }
        event.setCanceled(true);
    }
}
