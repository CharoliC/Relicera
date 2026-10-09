package com.yukari.relicera.common.curio;

import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.SyncAttackStrengthPacket;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.mixin.LivingEntityAccessor;
import com.yukari.relicera.registry.ModItems;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class BruteBeltEffects {
    private static final double BASE_DAMAGE_REDUCTION = 0.30D;
    private static final double SCALING_DAMAGE_REDUCTION = 0.45D;
    private static final double DAMAGE_SCALE = 8.0D;
    private static final double BASE_MAX_HEALTH = 20.0D;
    private static final double SOFT_CAP_MAX_HEALTH = 60.0D;
    private static final double SOFT_CAP_HEALTH_REDUCTION = 0.20D;
    private static final double HARD_CAP_HEALTH_REDUCTION = 0.25D;
    private static final UUID MAX_HEALTH_MODIFIER_ID =
            UUID.fromString("d42919f8-981e-4c34-9d45-c1dfc8987dc7");
    private static final String MAX_HEALTH_MODIFIER_NAME = "Relicera Belt of Brute max health";

    private BruteBeltEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.BRUTE_BELT.get()))
                .orElse(false);
    }

    public static void tickAttributes(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        AttributeModifier existing = maxHealth.getModifier(MAX_HEALTH_MODIFIER_ID);
        double amount = isEquipped(entity) ? ModCommonConfig.BRUTE_BELT_MAX_HEALTH_BONUS.get() : 0.0D;
        if (amount <= 0.0D) {
            if (existing != null) {
                maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
            }
            return;
        }

        if (existing != null
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL
                && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }
        maxHealth.addTransientModifier(new AttributeModifier(
                MAX_HEALTH_MODIFIER_ID,
                MAX_HEALTH_MODIFIER_NAME,
                amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    public static void reduceUnarmoredDamage(LivingHurtEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer.level().isClientSide()
                || event.isCanceled()
                || event.getAmount() <= 0.0F
                || !isEquipped(wearer)
                || !hasEmptyArmorSlots(wearer)) {
            return;
        }

        double damage = event.getAmount();
        double healthReduction = getMaxHealthDamageReduction(wearer.getMaxHealth());
        double reduction = BASE_DAMAGE_REDUCTION
                + SCALING_DAMAGE_REDUCTION * damage / (damage + DAMAGE_SCALE)
                + healthReduction * DAMAGE_SCALE / (damage + DAMAGE_SCALE);
        event.setAmount((float) (damage * (1.0D - Mth.clamp(reduction, 0.0D, 1.0D))));
    }

    public static void rechargeAttackAfterDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.isCanceled()
                || event.getAmount() <= 0.0F
                || !isEquipped(player)
                || !hasEmptyArmorSlots(player)
                || !hasExternalEntitySource(event.getSource(), player)) {
            return;
        }

        float attackDelay = player.getCurrentItemAttackStrengthDelay();
        if (!Float.isFinite(attackDelay)) {
            return;
        }

        LivingEntityAccessor accessor = (LivingEntityAccessor) player;
        int readyTicks = Math.max(accessor.relicera$getAttackStrengthTicker(), Mth.ceil(attackDelay));
        accessor.relicera$setAttackStrengthTicker(readyTicks);
        ModNetworking.sendToPlayer(new SyncAttackStrengthPacket(readyTicks), player);
    }

    private static boolean hasEmptyArmorSlots(LivingEntity entity) {
        for (ItemStack armorStack : entity.getArmorSlots()) {
            if (!armorStack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasExternalEntitySource(DamageSource source, Player player) {
        Entity causingEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();
        return (causingEntity != null || directEntity != null)
                && causingEntity != player
                && directEntity != player;
    }

    private static double getMaxHealthDamageReduction(double maxHealth) {
        if (maxHealth <= BASE_MAX_HEALTH) {
            return 0.0D;
        }
        if (maxHealth <= SOFT_CAP_MAX_HEALTH) {
            return SOFT_CAP_HEALTH_REDUCTION
                    * (maxHealth - BASE_MAX_HEALTH)
                    / (SOFT_CAP_MAX_HEALTH - BASE_MAX_HEALTH);
        }

        double reduction = SOFT_CAP_HEALTH_REDUCTION
                + (HARD_CAP_HEALTH_REDUCTION - SOFT_CAP_HEALTH_REDUCTION)
                * (maxHealth - SOFT_CAP_MAX_HEALTH)
                / (maxHealth - BASE_MAX_HEALTH);
        return Math.min(HARD_CAP_HEALTH_REDUCTION, reduction);
    }
}
