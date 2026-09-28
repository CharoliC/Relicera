package com.yukari.relicera.common.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;

public class EnvironmentallyIndestructibleItem extends Item {
    public EnvironmentallyIndestructibleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeHurtBy(DamageSource source) {
        return false;
    }
}
