package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class CovenantTabletEffects {
    private static final String FALL_IMMUNITY_UNLOCKED_TAG = "FallImmunityUnlocked";
    private static final String MINING_SPEED_UNLOCKED_TAG = "MiningSpeedUnlocked";
    private static final String VILLAGER_DISCOUNT_UNLOCKED_TAG = "VillagerDiscountUnlocked";

    private static final ConcurrentMap<UUID, Float> PENDING_HIGH_DAMAGE = new ConcurrentHashMap<>();

    private CovenantTabletEffects() {
    }

    public static Optional<ItemStack> getEquippedTablet(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .flatMap(handler -> handler.findCurios(ModItems.COVENANT_TABLET.get()).stream()
                        .map(result -> result.stack())
                        .filter(stack -> !stack.isEmpty())
                        .findFirst());
    }

    public static boolean hasFallImmunity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(FALL_IMMUNITY_UNLOCKED_TAG);
    }

    public static boolean hasMiningSpeed(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(MINING_SPEED_UNLOCKED_TAG);
    }

    public static boolean hasVillagerDiscount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(VILLAGER_DISCOUNT_UNLOCKED_TAG);
    }

    public static boolean hasFallImmunity(LivingEntity entity) {
        return getEquippedTablet(entity).map(CovenantTabletEffects::hasFallImmunity).orElse(false);
    }

    public static boolean hasMiningSpeed(LivingEntity entity) {
        return getEquippedTablet(entity).map(CovenantTabletEffects::hasMiningSpeed).orElse(false);
    }

    public static boolean hasVillagerDiscount(LivingEntity entity) {
        return getEquippedTablet(entity).map(CovenantTabletEffects::hasVillagerDiscount).orElse(false);
    }

    public static boolean isFullyUnlocked(ItemStack stack) {
        return hasFallImmunity(stack) && hasMiningSpeed(stack) && hasVillagerDiscount(stack);
    }

    public static ItemStack createFullyUnlockedStack() {
        ItemStack stack = new ItemStack(ModItems.COVENANT_TABLET.get());
        unlockFallImmunity(stack);
        unlockMiningSpeed(stack);
        unlockVillagerDiscount(stack);
        return stack;
    }

    public static void rememberHighDamageCandidate(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PENDING_HIGH_DAMAGE.remove(player.getUUID());
        Optional<ItemStack> tablet = getEquippedTablet(player);
        if (tablet.isEmpty() || hasFallImmunity(tablet.get())) {
            return;
        }

        if (event.getAmount() > ModCommonConfig.COVENANT_TABLET_DAMAGE_TASK_THRESHOLD.get()) {
            PENDING_HIGH_DAMAGE.put(player.getUUID(), event.getAmount());
        }
    }

    public static void completeHighDamageTask(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Float originalDamage = PENDING_HIGH_DAMAGE.remove(player.getUUID());
        if (originalDamage == null) {
            return;
        }

        if (event.getAmount() < player.getHealth() + player.getAbsorptionAmount()) {
            getEquippedTablet(player).ifPresent(CovenantTabletEffects::unlockFallImmunity);
        }
    }

    public static boolean preventFallDamage(LivingAttackEvent event) {
        if (event.getSource().is(DamageTypes.FALL) && hasFallImmunity(event.getEntity())) {
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public static void applyMiningSpeed(PlayerEvent.BreakSpeed event) {
        if (hasMiningSpeed(event.getEntity())) {
            event.setNewSpeed(event.getNewSpeed() * (1.0F + (float) ModCommonConfig.COVENANT_TABLET_MINING_SPEED_BONUS.get().doubleValue()));
        }
    }

    public static void completeToolsmithTradeTask(TradeWithVillagerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getAbstractVillager() instanceof Villager villager)) {
            return;
        }

        VillagerData villagerData = villager.getVillagerData();
        if (villagerData.getProfession() == VillagerProfession.TOOLSMITH && villagerData.getLevel() == VillagerData.MAX_VILLAGER_LEVEL) {
            getEquippedTablet(player).ifPresent(CovenantTabletEffects::unlockMiningSpeed);
        }
    }

    public static void completeRaidTask(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getEffectInstance().getEffect() != MobEffects.HERO_OF_THE_VILLAGE) {
            return;
        }

        getEquippedTablet(player).ifPresent(CovenantTabletEffects::unlockVillagerDiscount);
    }

    private static void unlockFallImmunity(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(FALL_IMMUNITY_UNLOCKED_TAG, true);
    }

    private static void unlockMiningSpeed(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(MINING_SPEED_UNLOCKED_TAG, true);
    }

    private static void unlockVillagerDiscount(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(VILLAGER_DISCOUNT_UNLOCKED_TAG, true);
    }
}
