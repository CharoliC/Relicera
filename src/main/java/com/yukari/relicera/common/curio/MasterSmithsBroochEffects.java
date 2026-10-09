package com.yukari.relicera.common.curio;

import com.yukari.relicera.common.item.feysilver.FeysilverCoating;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public final class MasterSmithsBroochEffects {
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_ID = UUID.fromString("5e50f7ea-1961-41bd-8752-54fe73be41db");
    private static final String ARMOR_TOUGHNESS_MODIFIER_NAME = "Relicera Master Smith's Brooch armor toughness";

    private MasterSmithsBroochEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.MASTER_SMITHS_BROOCH.get()))
                .orElse(false);
    }

    public static void tickAttributes(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        boolean equipped = isEquipped(entity);
        applyAttribute(
                entity,
                Attributes.ARMOR_TOUGHNESS,
                ARMOR_TOUGHNESS_MODIFIER_ID,
                ARMOR_TOUGHNESS_MODIFIER_NAME,
                equipped ? getArmorToughnessBonus(entity) : 0.0D
        );
    }

    public static void reduceDamage(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !isEquipped(entity)) {
            return;
        }

        double reduction = getDamageReduction(entity);
        if (reduction > 0.0D) {
            event.setAmount((float) (event.getAmount() * (1.0D - reduction)));
        }
    }

    public static double getArmorToughnessBonus(LivingEntity entity) {
        return countTrimmedArmor(entity)
                * ModCommonConfig.MASTER_SMITHS_BROOCH_ARMOR_TOUGHNESS_PER_TRIMMED_ARMOR.get();
    }

    public static double getDamageReduction(LivingEntity entity) {
        return Math.min(
                1.0D,
                countCoatedArmor(entity) * ModCommonConfig.MASTER_SMITHS_BROOCH_DAMAGE_REDUCTION_PER_COATED_ARMOR.get()
        );
    }

    private static int countTrimmedArmor(LivingEntity entity) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (isArmor(stack) && ArmorTrim.getTrim(entity.level().registryAccess(), stack).isPresent()) {
                count++;
            }
        }
        return count;
    }

    private static int countCoatedArmor(LivingEntity entity) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (isArmor(stack) && FeysilverCoating.hasCoating(stack)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    private static void applyAttribute(LivingEntity entity, Attribute attribute, UUID id, String name, double amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(id);
        if (amount <= 0.0D) {
            if (existing != null) {
                instance.removeModifier(id);
            }
            return;
        }

        if (existing != null && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
    }
}
