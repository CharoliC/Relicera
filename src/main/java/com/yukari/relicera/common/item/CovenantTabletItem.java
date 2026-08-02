package com.yukari.relicera.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.yukari.relicera.registry.ModItems;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

public class CovenantTabletItem extends QuickEquipCurioItem {
    private static final String CHARM_SLOT = "charm";
    private static final UUID CHARM_SLOT_MODIFIER_UUID = UUID.fromString("e43502ef-f0ff-4b5d-87a3-7393c044b819");

    public CovenantTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (!CHARM_SLOT.equals(slotContext.identifier())) {
            return false;
        }

        if (slotContext.entity() == null) {
            return super.canEquip(slotContext, stack);
        }

        return CuriosApi.getCuriosInventory(slotContext.entity())
                .resolve()
                .map(handler -> handler.findCurios(ModItems.COVENANT_TABLET.get()).stream()
                        .noneMatch(result -> result.stack() != stack))
                .orElse(true);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create(super.getAttributeModifiers(slotContext, uuid, stack));
        CuriosApi.addSlotModifier(
                modifiers,
                CHARM_SLOT,
                CHARM_SLOT_MODIFIER_UUID,
                1.0D,
                AttributeModifier.Operation.ADDITION
        );
        return modifiers;
    }
}
