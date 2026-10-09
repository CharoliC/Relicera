package com.yukari.relicera.common.curio;

import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.CameraShakePacket;
import com.yukari.relicera.common.network.packet.ThousandweightGauntletsSmashPacket;
import com.yukari.relicera.common.item.ThousandweightGauntletsItem;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.UUID;

public final class ThousandweightGauntletsEffects {
    public static final float SMASH_MINIMUM_FALL_DISTANCE = 1.5F;
    private static final double SMASH_KNOCKBACK_RADIUS = 3.5D;
    private static final double SMASH_KNOCKBACK_STRENGTH = 0.7D;
    private static final double SMASH_REBOUND_Y = 0.01D;
    private static final double SMASH_PARTICLE_VIEW_DISTANCE = 64.0D;
    private static final int SMASH_CAMERA_SHAKE_DURATION_TICKS = 8;
    private static final float SMASH_CAMERA_SHAKE_INTENSITY = 1.5F;
    private static final float FULL_STRENGTH_ATTACK_THRESHOLD = 0.9F;
    private static final int FULL_STRENGTH_EFFECT_DURATION_TICKS = 5 * 20;
    private static final int SLOWNESS_III_AMPLIFIER = 2;
    private static final double VERTICAL_AIR_DRAG = 0.98D;
    private static final int LAUNCH_VELOCITY_SEARCH_STEPS = 32;
    private static final int MAX_APEX_SIMULATION_TICKS = 512;
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("c6e17f49-bb8a-43f0-8e16-0d1f1f53b5fb");
    private static final String ATTACK_SPEED_MODIFIER_NAME = "Relicera Thousandweight Gauntlets attack speed";

    private ThousandweightGauntletsEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.THOUSANDWEIGHT_GAUNTLETS.get()))
                .orElse(false);
    }

    public static void tickAttackSpeed(ServerPlayer player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }

        AttributeModifier existing = attackSpeed.getModifier(ATTACK_SPEED_MODIFIER_ID);
        double reduction = ModCommonConfig.THOUSANDWEIGHT_GAUNTLETS_ATTACK_SPEED_REDUCTION.get();
        if (!isEquipped(player) || reduction <= 0.0D) {
            if (existing != null) {
                attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
            }
            return;
        }

        double amount = -reduction;
        if (existing != null
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL
                && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
        attackSpeed.addTransientModifier(new AttributeModifier(
                ATTACK_SPEED_MODIFIER_ID,
                ATTACK_SPEED_MODIFIER_NAME,
                amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    public static float getSmashFallDistance(Player player, Entity target) {
        if (!(target instanceof LivingEntity)
                || player.fallDistance <= SMASH_MINIMUM_FALL_DISTANCE
                || player.isFallFlying()
                || !isEquipped(player)) {
            return 0.0F;
        }
        return player.fallDistance;
    }

    public static float getSmashDamageBonus(float fallDistance) {
        return Math.min(6.0F, fallDistance * 2.0F)
                + Math.min(8.0F, fallDistance)
                + fallDistance;
    }

    public static void handleSuccessfulAttack(Player player, Entity target, float smashFallDistance) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        applyFullyChargedAttackEffects(serverPlayer, target);
        if (smashFallDistance > SMASH_MINIMUM_FALL_DISTANCE) {
            handleSuccessfulSmash(serverPlayer, target, smashFallDistance);
        }
    }

    private static void applyFullyChargedAttackEffects(ServerPlayer player, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)
                || player.getAttackStrengthScale(0.5F) <= FULL_STRENGTH_ATTACK_THRESHOLD
                || !isEquipped(player)) {
            return;
        }

        livingTarget.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                FULL_STRENGTH_EFFECT_DURATION_TICKS,
                SLOWNESS_III_AMPLIFIER
        ), player);
    }

    private static void handleSuccessfulSmash(ServerPlayer player, Entity struckEntity, float fallDistance) {
        double windBurstHeight = consumeWindBurstLaunchHeight(player);
        reboundAndResetFall(player, windBurstHeight);
        if (windBurstHeight > 0.0D) {
            BaromAdvancements.recordWindBurst(player);
        }
        knockBackNearbyEntities(player, struckEntity, fallDistance);
        playSmashEffects(player, struckEntity, windBurstHeight > 0.0D);
    }

    private static double consumeWindBurstLaunchHeight(ServerPlayer player) {
        if (player.isShiftKeyDown()) {
            return 0.0D;
        }

        double height = ModCommonConfig.THOUSANDWEIGHT_GAUNTLETS_WIND_BURST_LAUNCH_HEIGHT.get();
        return height > 0.0D && consumeWindBurstCharge(player) ? height : 0.0D;
    }

    private static void playSmashEffects(ServerPlayer player, Entity struckEntity, boolean windBurst) {
        ServerLevel level = player.serverLevel();
        level.playSound(
                null,
                struckEntity.getX(),
                struckEntity.getY(),
                struckEntity.getZ(),
                SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS,
                1.0F,
                0.65F
        );

        BlockState particleState = findImpactParticleState(level, struckEntity);
        ModNetworking.sendToPlayer(new CameraShakePacket(
                SMASH_CAMERA_SHAKE_DURATION_TICKS,
                SMASH_CAMERA_SHAKE_INTENSITY
        ), player);
        ThousandweightGauntletsSmashPacket impact = new ThousandweightGauntletsSmashPacket(
                struckEntity.getX(),
                struckEntity.getY() + 0.1D,
                struckEntity.getZ(),
                Block.getId(particleState),
                windBurst
        );
        double viewDistanceSquared = SMASH_PARTICLE_VIEW_DISTANCE * SMASH_PARTICLE_VIEW_DISTANCE;
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(struckEntity) <= viewDistanceSquared) {
                ModNetworking.sendToPlayer(impact, viewer);
            }
        }
    }

    private static void reboundAndResetFall(ServerPlayer player, double windBurstHeight) {
        Vec3 movement = player.getDeltaMovement();
        double reboundY = windBurstHeight > 0.0D
                ? calculateLaunchVelocity(player, windBurstHeight)
                : SMASH_REBOUND_Y;
        player.setDeltaMovement(movement.x, reboundY, movement.z);
        player.resetFallDistance();
        syncPlayerMotion(player);
    }

    private static void syncPlayerMotion(ServerPlayer player) {
        player.hurtMarked = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }

    private static boolean consumeWindBurstCharge(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> {
                    for (SlotResult result : handler.findCurios(ModItems.THOUSANDWEIGHT_GAUNTLETS.get())) {
                        ItemStack chargedGauntlets = result.stack();
                        if (ThousandweightGauntletsItem.getWindBurstCharge(chargedGauntlets) <= 0) {
                            continue;
                        }

                        if (!player.getAbilities().instabuild) {
                            ItemStack updatedGauntlets = chargedGauntlets.copy();
                            ThousandweightGauntletsItem.consumeWindBurstCharge(updatedGauntlets);
                            handler.setEquippedCurio(
                                    result.slotContext().identifier(),
                                    result.slotContext().index(),
                                    updatedGauntlets
                            );
                        }
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private static double calculateLaunchVelocity(ServerPlayer player, double targetHeight) {
        double gravity = Math.max(0.001D, player.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get()));
        double lowerBound = 0.0D;
        double upperBound = Math.max(1.0D, Math.sqrt(2.0D * gravity * targetHeight) * 2.0D);
        while (simulateApexHeight(upperBound, gravity) < targetHeight) {
            upperBound *= 2.0D;
        }

        for (int step = 0; step < LAUNCH_VELOCITY_SEARCH_STEPS; step++) {
            double candidate = (lowerBound + upperBound) * 0.5D;
            if (simulateApexHeight(candidate, gravity) < targetHeight) {
                lowerBound = candidate;
            } else {
                upperBound = candidate;
            }
        }
        return upperBound;
    }

    private static double simulateApexHeight(double initialVelocity, double gravity) {
        double height = 0.0D;
        double velocity = initialVelocity;
        for (int tick = 0; tick < MAX_APEX_SIMULATION_TICKS && velocity > 0.0D; tick++) {
            height += velocity;
            velocity = (velocity - gravity) * VERTICAL_AIR_DRAG;
        }
        return height;
    }

    private static void knockBackNearbyEntities(ServerPlayer attacker, Entity struckEntity, float fallDistance) {
        double strengthMultiplier = fallDistance > 5.0F ? 2.0D : 1.0D;
        double radiusSquared = SMASH_KNOCKBACK_RADIUS * SMASH_KNOCKBACK_RADIUS;

        for (LivingEntity nearby : attacker.level().getEntitiesOfClass(
                LivingEntity.class,
                struckEntity.getBoundingBox().inflate(SMASH_KNOCKBACK_RADIUS),
                candidate -> canBeKnockedBack(attacker, struckEntity, candidate)
                        && candidate.distanceToSqr(struckEntity) <= radiusSquared
        )) {
            Vec3 offset = nearby.position().subtract(struckEntity.position());
            double distance = offset.length();
            double resistance = nearby.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            double power = (SMASH_KNOCKBACK_RADIUS - distance)
                    * SMASH_KNOCKBACK_STRENGTH
                    * strengthMultiplier
                    * (1.0D - resistance);
            Vec3 direction = offset.normalize();
            nearby.push(direction.x * power, SMASH_KNOCKBACK_STRENGTH, direction.z * power);

            if (nearby instanceof ServerPlayer serverPlayer) {
                syncPlayerMotion(serverPlayer);
            }
        }
    }

    private static boolean canBeKnockedBack(ServerPlayer attacker, Entity struckEntity, LivingEntity candidate) {
        if (candidate == attacker
                || candidate == struckEntity
                || candidate.isSpectator()
                || attacker.isAlliedTo(candidate)) {
            return false;
        }
        if (candidate instanceof ArmorStand armorStand && armorStand.isMarker()) {
            return false;
        }
        return !(candidate instanceof TamableAnimal tamableAnimal
                && tamableAnimal.isTame()
                && attacker.getUUID().equals(tamableAnimal.getOwnerUUID()));
    }

    private static BlockState findImpactParticleState(ServerLevel level, Entity struckEntity) {
        BlockPos origin = struckEntity.blockPosition();
        for (int offset = 0; offset <= 3; offset++) {
            BlockState state = level.getBlockState(origin.below(offset));
            if (!state.isAir()) {
                return state;
            }
        }
        return level.getBlockState(origin);
    }
}
