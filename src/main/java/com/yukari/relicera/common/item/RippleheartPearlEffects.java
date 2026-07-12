package com.yukari.relicera.common.item;

import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.mixin.FoxAccessor;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class RippleheartPearlEffects {
    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("4308e7d7-a4e2-4d0c-a0cb-df09ecfe9d18");
    private static final String MAX_HEALTH_MODIFIER_NAME = "Relicera rippleheart pearl max health";
    private static final String HEALTH_BONUS_TAG = "ReliceraRippleheartHealthBonus";
    private static final double HEALTH_BONUS_PER_FEED = 20.0D;
    private static final double MAX_HEALTH_BONUS = 20.0D;
    private static final int REGENERATION_DURATION_TICKS = 10 * 20;

    private RippleheartPearlEffects() {
    }

    public static void feedEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.RIPPLEHEART_PEARL.get()) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        Player player = event.getEntity();
        if (!canFeed(player, target) || !canApplyAnyEffect(player, target)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));

        if (player.level().isClientSide()) {
            return;
        }

        boolean changed = applyHealthBonus(target);
        if (target instanceof Fox fox && !trusts(fox, player)) {
            ((FoxAccessor) fox).relicera$addTrustedUUID(player.getUUID());
            changed = true;
        }

        if (!changed) {
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_DURATION_TICKS, 0, false, true, true));
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 1.2F);
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(target, (byte) 7);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static boolean canFeed(Player player, LivingEntity target) {
        if (target instanceof Fox) {
            return true;
        }
        if (target instanceof Horse || target instanceof Donkey || target instanceof Mule) {
            return target instanceof AbstractHorse horse && horse.isTamed();
        }
        if (target instanceof Wolf || target instanceof Cat) {
            return target instanceof TamableAnimal tamable && tamable.isTame() && tamable.isOwnedBy(player);
        }
        return false;
    }

    private static boolean canApplyAnyEffect(Player player, LivingEntity target) {
        if (getCurrentHealthBonus(target) < MAX_HEALTH_BONUS) {
            return true;
        }
        return target instanceof Fox fox && !trusts(fox, player);
    }

    private static boolean trusts(Fox fox, Player player) {
        return ((FoxAccessor) fox).relicera$trusts(player.getUUID());
    }

    private static boolean applyHealthBonus(LivingEntity target) {
        double currentBonus = getCurrentHealthBonus(target);
        if (currentBonus >= MAX_HEALTH_BONUS) {
            return false;
        }

        double newBonus = Math.min(MAX_HEALTH_BONUS, currentBonus + HEALTH_BONUS_PER_FEED);
        target.getPersistentData().putDouble(HEALTH_BONUS_TAG, newBonus);

        AttributeInstance maxHealth = target.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return false;
        }

        AttributeModifier existing = maxHealth.getModifier(MAX_HEALTH_MODIFIER_ID);
        if (existing != null) {
            maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }
        maxHealth.addPermanentModifier(new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID,
                MAX_HEALTH_MODIFIER_NAME,
                newBonus,
                AttributeModifier.Operation.ADDITION
        ));
        return true;
    }

    private static double getCurrentHealthBonus(LivingEntity target) {
        return Math.min(MAX_HEALTH_BONUS, Math.max(0.0D, target.getPersistentData().getDouble(HEALTH_BONUS_TAG)));
    }
}
