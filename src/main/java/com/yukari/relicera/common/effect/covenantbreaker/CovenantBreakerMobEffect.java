package com.yukari.relicera.common.effect.covenantbreaker;

import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

public final class CovenantBreakerMobEffect extends MobEffect {
    public CovenantBreakerMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0x535458);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
