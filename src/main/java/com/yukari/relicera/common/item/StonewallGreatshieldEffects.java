package com.yukari.relicera.common.item;

import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.CameraShakePacket;
import com.yukari.relicera.mixin.ExplosionAccessor;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.event.level.ExplosionEvent;

import java.util.UUID;

public final class StonewallGreatshieldEffects {
    private static final AttributeModifier BASH_KNOCKBACK = new AttributeModifier(
            UUID.fromString("a9f231d9-63cd-4418-90f9-eecf3a769020"),
            "Relicera Stonewall Greatshield bash knockback",
            1.5D,
            AttributeModifier.Operation.ADDITION
    );
    private static final int RESIST_SHAKE_DURATION_TICKS = 6;
    private static final float RESIST_SHAKE_INTENSITY = 0.85F;
    private static final int RESIST_SHAKE_DEBOUNCE_TICKS = 5;
    private static final String LAST_RESIST_SHAKE_TICK_TAG = "ReliceraStonewallLastResistShakeTick";

    private StonewallGreatshieldEffects() {
    }

    public static boolean isHeld(LivingEntity entity) {
        return entity.getMainHandItem().is(ModItems.STONEWALL_GREATSHIELD.get())
                || entity.getOffhandItem().is(ModItems.STONEWALL_GREATSHIELD.get());
    }

    public static boolean isUsing(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().is(ModItems.STONEWALL_GREATSHIELD.get());
    }

    public static boolean isUsingInMainHand(LivingEntity entity) {
        return isUsing(entity) && entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
    }

    public static boolean canBash(Player player) {
        return isUsingInMainHand(player) && player.isBlocking() && player.hasEffect(MobEffects.DAMAGE_BOOST);
    }

    public static void beginBash(Player player) {
        player.setSprinting(false);
        AttributeInstance knockback = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (knockback != null) {
            knockback.removeModifier(BASH_KNOCKBACK.getId());
            knockback.addTransientModifier(BASH_KNOCKBACK);
        }
    }

    public static void endBash(Player player) {
        AttributeInstance knockback = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (knockback != null) {
            knockback.removeModifier(BASH_KNOCKBACK.getId());
        }
    }

    public static void clearCooldownWhileHeld(Player player) {
        if (isHeld(player) && player.getCooldowns().isOnCooldown(ModItems.STONEWALL_GREATSHIELD.get())) {
            player.getCooldowns().removeCooldown(ModItems.STONEWALL_GREATSHIELD.get());
            triggerResistedShieldDisable(player);
        }
    }

    public static void triggerResistedShieldDisable(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        long gameTime = player.level().getGameTime();
        if (data.contains(LAST_RESIST_SHAKE_TICK_TAG)) {
            long elapsed = gameTime - data.getLong(LAST_RESIST_SHAKE_TICK_TAG);
            if (elapsed >= 0L && elapsed < RESIST_SHAKE_DEBOUNCE_TICKS) {
                return;
            }
        }

        data.putLong(LAST_RESIST_SHAKE_TICK_TAG, gameTime);
        ModNetworking.sendToPlayer(
                new CameraShakePacket(RESIST_SHAKE_DURATION_TICKS, RESIST_SHAKE_INTENSITY),
                serverPlayer
        );
    }

    public static void protectBlocksFromExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide() || event.getAffectedBlocks().isEmpty()) {
            return;
        }

        Explosion explosion = event.getExplosion();

        double entityReach = ((ExplosionAccessor) explosion).relicera$getRadius() * 2.0D;
        double reachSquared = entityReach * entityReach;
        for (Entity entity : event.getAffectedEntities()) {
            if (entity instanceof ServerPlayer player
                    && !player.isSpectator()
                    && !player.ignoreExplosion()
                    && isHeld(player)
                    && player.distanceToSqr(explosion.getPosition()) <= reachSquared) {
                event.getAffectedBlocks().clear();
                return;
            }
        }
    }
}
