package com.yukari.relicera.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class LeatherGlovesItem extends QuickEquipCurioItem {
    private static final double ARMOR_BONUS = 2.0D;

    public LeatherGlovesItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid,
                                                                        ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers =
                HashMultimap.create(super.getAttributeModifiers(slotContext, uuid, stack));
        modifiers.put(Attributes.ARMOR, new AttributeModifier(
                uuid,
                "Relicera Leather Gloves armor",
                ARMOR_BONUS,
                AttributeModifier.Operation.ADDITION
        ));
        return modifiers;
    }
}
