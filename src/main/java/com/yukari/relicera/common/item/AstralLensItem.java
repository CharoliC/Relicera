package com.yukari.relicera.common.item;

import com.yukari.relicera.common.astral.AstralLensEntityBehavior;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AstralLensItem extends Item {
    public AstralLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity itemEntity) {
        return AstralLensEntityBehavior.tick(itemEntity);
    }
}
