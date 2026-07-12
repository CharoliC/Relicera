package com.yukari.relicera.common.item;

import com.yukari.relicera.common.effect.TempestSprintEffects;
import com.yukari.relicera.registry.ModEffects;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

public final class TempestsReinsEffects {
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("7f189f7b-ea8f-42ce-9bf0-d2cd1e7f7f01");
    private static final UUID JUMP_STRENGTH_MODIFIER_ID = UUID.fromString("10569c5b-ddd2-4d35-ad4d-57cfed65ba2d");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_ID = UUID.fromString("2a5e43c6-45d5-485f-a7c6-13de6d7ab5de");
    private static final UUID STEP_HEIGHT_MODIFIER_ID = UUID.fromString("f0179a68-13f2-4764-b6fb-f22d32efe647");

    public static final double MOVEMENT_SPEED_BONUS = 0.30D;
    public static final double JUMP_STRENGTH_BONUS = 0.50D;
    public static final double STEP_HEIGHT_BONUS = 1.0D;
    private static final double ARMOR_TOUGHNESS_BONUS = 4.0D;
    private static final int TEMPEST_SPRINT_DURATION_TICKS = 60 * 20;
    private static final double WATER_SURFACE_Y_OFFSET = 1.0D;
    private static final double WATER_SURFACE_SEARCH_BELOW = 0.08D;
    private static final double WATER_SURFACE_SNAP_RANGE = 0.45D;

    private TempestsReinsEffects() {
    }

    public static void tickHorse(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            boolean equipped = hasTempestsReins(horse);
            applyModifier(horse.getAttribute(Attributes.MOVEMENT_SPEED), MOVEMENT_SPEED_MODIFIER_ID,
                    "Tempest's Reins movement speed", MOVEMENT_SPEED_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL, equipped);
            applyModifier(horse.getAttribute(Attributes.JUMP_STRENGTH), JUMP_STRENGTH_MODIFIER_ID,
                    "Tempest's Reins jump strength", JUMP_STRENGTH_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL, equipped);
            applyModifier(horse.getAttribute(Attributes.ARMOR_TOUGHNESS), ARMOR_TOUGHNESS_MODIFIER_ID,
                    "Tempest's Reins armor toughness", ARMOR_TOUGHNESS_BONUS, AttributeModifier.Operation.ADDITION, equipped);
            applyModifier(horse.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()), STEP_HEIGHT_MODIFIER_ID,
                    "Tempest's Reins step height", STEP_HEIGHT_BONUS, AttributeModifier.Operation.ADDITION, equipped);

            if (equipped) {
                if (horse.isOnFire()) {
                    horse.clearFire();
                }
                standOnWater(horse);
            }
        }
    }

    public static boolean preventDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof Horse horse && hasTempestsReins(horse) && isPreventedDamage(event)) {
            grantTempestSprintIfLightning(horse, event.getSource().is(DamageTypeTags.IS_LIGHTNING));
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public static boolean preventDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof Horse horse && hasTempestsReins(horse) && isPreventedDamage(event)) {
            grantTempestSprintIfLightning(horse, event.getSource().is(DamageTypeTags.IS_LIGHTNING));
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    private static boolean isPreventedDamage(LivingAttackEvent event) {
        return event.getSource().is(DamageTypeTags.IS_FALL)
                || event.getSource().is(DamageTypeTags.IS_LIGHTNING)
                || event.getSource().is(DamageTypeTags.IS_FIRE);
    }

    private static boolean isPreventedDamage(LivingHurtEvent event) {
        return event.getSource().is(DamageTypeTags.IS_FALL)
                || event.getSource().is(DamageTypeTags.IS_LIGHTNING)
                || event.getSource().is(DamageTypeTags.IS_FIRE);
    }

    public static boolean hasTempestsReins(Horse horse) {
        ItemStack armor = horse.getArmor();
        return armor.is(ModItems.TEMPESTS_REINS.get());
    }

    public static boolean allowsWaterStanding(LivingEntity entity) {
        return entity instanceof Horse horse && hasTempestsReins(horse);
    }

    public static boolean shouldIgnoreWaterState(LivingEntity entity) {
        return entity instanceof Horse horse && hasTempestsReins(horse) && getWaterSurfacePos(horse) != null;
    }

    private static void standOnWater(Horse horse) {
        BlockPos waterPos = getWaterSurfacePos(horse);
        if (waterPos == null) {
            return;
        }

        CollisionContext collisionContext = CollisionContext.of(horse);
        if (collisionContext.isAbove(LiquidBlock.STABLE_SHAPE, waterPos, true)) {
            snapToWaterSurface(horse, waterPos);
            horse.setOnGround(true);
        }
    }

    private static BlockPos getWaterSurfacePos(Horse horse) {
        BlockPos pos = BlockPos.containing(
                horse.getX(),
                horse.getBoundingBox().minY - WATER_SURFACE_SEARCH_BELOW,
                horse.getZ()
        );
        if (horse.level().getFluidState(pos).is(FluidTags.WATER)
                && !horse.level().getFluidState(pos.above()).is(FluidTags.WATER)) {
            return pos;
        }
        return null;
    }

    private static void snapToWaterSurface(Horse horse, BlockPos waterPos) {
        double surfaceY = waterPos.getY() + WATER_SURFACE_Y_OFFSET;
        double deltaY = surfaceY - horse.getBoundingBox().minY;
        if (Math.abs(deltaY) <= WATER_SURFACE_SNAP_RANGE && Math.abs(deltaY) > 1.0E-4D && horse.getDeltaMovement().y <= 0.0D) {
            horse.setPos(horse.getX(), horse.getY() + deltaY, horse.getZ());
        }
        if (horse.getDeltaMovement().y < 0.0D) {
            horse.setDeltaMovement(horse.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
        }
    }

    private static void grantTempestSprintIfLightning(Horse horse, boolean lightningDamage) {
        if (!lightningDamage || horse.level().isClientSide()) {
            return;
        }

        horse.addEffect(new MobEffectInstance(ModEffects.TEMPEST_SPRINT.get(), TEMPEST_SPRINT_DURATION_TICKS, 0, false, true, true));
    }

    private static void applyModifier(AttributeInstance instance, UUID modifierId, String name, double amount,
                                      AttributeModifier.Operation operation, boolean shouldApply) {
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(modifierId);
        if (shouldApply && existing != null) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(modifierId);
        }
        if (shouldApply) {
            instance.addTransientModifier(new AttributeModifier(modifierId, name, amount, operation));
        }
    }
}
