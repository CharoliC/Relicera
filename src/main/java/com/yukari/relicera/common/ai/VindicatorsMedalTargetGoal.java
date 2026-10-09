package com.yukari.relicera.common.ai;

import com.yukari.relicera.common.curio.VindicatorsMedalEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;

public class VindicatorsMedalTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
    public VindicatorsMedalTargetGoal(IronGolem golem) {
        super(golem, LivingEntity.class, 10, true, false, VindicatorsMedalEffects::isEquipped);
        this.targetConditions = TargetingConditions.forNonCombat()
                .range(this.getFollowDistance())
                .selector(entity -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
                        && !golem.isAlliedTo(entity)
                        && VindicatorsMedalEffects.isEquipped(entity));
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null
                && VindicatorsMedalEffects.isEquipped(target)
                && super.canContinueToUse();
    }
}
