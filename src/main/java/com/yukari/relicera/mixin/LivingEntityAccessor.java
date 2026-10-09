package com.yukari.relicera.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastHurt")
    float relicera$getLastHurt();

    @Accessor("lastHurt")
    void relicera$setLastHurt(float lastHurt);

    @Accessor("attackStrengthTicker")
    int relicera$getAttackStrengthTicker();

    @Accessor("attackStrengthTicker")
    void relicera$setAttackStrengthTicker(int attackStrengthTicker);
}
