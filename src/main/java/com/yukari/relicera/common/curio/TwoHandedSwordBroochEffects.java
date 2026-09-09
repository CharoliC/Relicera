package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public final class TwoHandedSwordBroochEffects {
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("7fc1b920-3d8a-4ec0-8726-0db49803814a");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("e29cc451-c194-46fb-9d25-634587c87675");
    private static final UUID ENTITY_REACH_MODIFIER_ID = UUID.fromString("cb45a942-32c7-4a5c-90e2-aa598305de5f");
    private static final String ATTACK_DAMAGE_MODIFIER_NAME = "Relicera Two-Handed Sword Brooch attack damage";
    private static final String ATTACK_SPEED_MODIFIER_NAME = "Relicera Two-Handed Sword Brooch attack speed";
    private static final String ENTITY_REACH_MODIFIER_NAME = "Relicera Two-Handed Sword Brooch entity reach";

    private TwoHandedSwordBroochEffects() {
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.TWO_HANDED_SWORD_BROOCH.get()))
                .orElse(false);
    }

    public static boolean hasSwordActive(Player player) {
        return isEquipped(player) && player.getMainHandItem().is(ItemTags.SWORDS);
    }

    public static void tickPlayer(ServerPlayer player) {
        boolean equipped = isEquipped(player);
        boolean swordActive = equipped && player.getMainHandItem().is(ItemTags.SWORDS);

        applyAttribute(
                player,
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_ID,
                ATTACK_DAMAGE_MODIFIER_NAME,
                swordActive,
                ModCommonConfig.TWO_HANDED_SWORD_BROOCH_SWORD_DAMAGE_BONUS.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        applyAttribute(
                player,
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER_ID,
                ATTACK_SPEED_MODIFIER_NAME,
                swordActive,
                -ModCommonConfig.TWO_HANDED_SWORD_BROOCH_ATTACK_SPEED_PENALTY.get(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        applyAttribute(
                player,
                ForgeMod.ENTITY_REACH.get(),
                ENTITY_REACH_MODIFIER_ID,
                ENTITY_REACH_MODIFIER_NAME,
                equipped,
                ModCommonConfig.TWO_HANDED_SWORD_BROOCH_ENTITY_REACH_BONUS.get(),
                AttributeModifier.Operation.ADDITION
        );

        if (equipped) {
            dropOffhandItem(player);
        }
    }

    public static void preventHandSwap(LivingSwapItemsEvent.Hands event) {
        if (event.getEntity() instanceof Player player && isEquipped(player)) {
            event.setCanceled(true);
        }
    }

    private static void dropOffhandItem(ServerPlayer player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty()) {
            return;
        }

        ItemStack dropped = offhand.copy();
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.drop(dropped, false);
    }

    private static void applyAttribute(Player player, Attribute attribute, UUID id, String name,
                                       boolean active, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(id);
        if (!active) {
            if (existing != null) {
                instance.removeModifier(id);
            }
            return;
        }

        if (existing != null
                && existing.getOperation() == operation
                && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }
}
