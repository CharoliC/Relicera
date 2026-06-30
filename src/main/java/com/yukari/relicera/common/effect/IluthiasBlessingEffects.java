package com.yukari.relicera.common.effect;

import com.yukari.relicera.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class IluthiasBlessingEffects {
    private IluthiasBlessingEffects() {
    }

    public static boolean preventDamage(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModEffects.ILUTHIAS_BLESSING.get())) {
            return false;
        }

        float amount = event.getAmount();
        if (amount > 0.0F) {
            entity.setAbsorptionAmount(entity.getAbsorptionAmount() + amount);
        }
        event.setCanceled(true);
        return true;
    }
}
