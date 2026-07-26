package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

public final class BrutalPlunderBadgeEffects {
    private BrutalPlunderBadgeEffects() {
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.BRUTAL_PLUNDER_BADGE.get()))
                .orElse(false);
    }

    public static void applyLootingBonus(LootingLevelEvent event) {
        if (event.getDamageSource() != null
                && event.getDamageSource().getEntity() instanceof Player player
                && isEquipped(player)) {
            event.setLootingLevel(event.getLootingLevel() + ModCommonConfig.BRUTAL_PLUNDER_BADGE_LOOTING_BONUS.get());
        }
    }

    public static void applyDamageBonus(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !isEquipped(player)) {
            return;
        }

        int lootingLevel = ForgeHooks.getLootingLevel(event.getEntity(), player, event.getSource());
        if (lootingLevel <= 0) {
            return;
        }

        double damageBonus = ModCommonConfig.BRUTAL_PLUNDER_BADGE_DAMAGE_BONUS_PER_LOOTING_LEVEL.get() * lootingLevel;
        float multiplier = 1.0F + (float) Math.min(damageBonus, ModCommonConfig.BRUTAL_PLUNDER_BADGE_MAX_DAMAGE_BONUS.get());
        event.setAmount(event.getAmount() * multiplier);
    }

    public static void addPiglinBarterDrop(LivingDropsEvent event) {
        if (event.getEntity().getType() != EntityType.PIGLIN
                || !(event.getEntity() instanceof Piglin piglin)
                || !(event.getSource().getEntity() instanceof Player player)
                || !isEquipped(player)
                || !(piglin.level() instanceof ServerLevel level)) {
            return;
        }

        List<ItemStack> barterResult = getBarterResult(level, piglin);
        if (barterResult.isEmpty()) {
            return;
        }

        int rolls = getExtraBarterRolls(event.getLootingLevel(), level);
        for (int i = 0; i < rolls; i++) {
            for (ItemStack stack : barterResult) {
                if (!stack.isEmpty()) {
                    event.getDrops().add(new ItemEntity(
                            level,
                            piglin.getX(),
                            piglin.getY(),
                            piglin.getZ(),
                            stack.copy()
                    ));
                }
            }
        }
    }

    private static List<ItemStack> getBarterResult(ServerLevel level, Piglin piglin) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
        LootParams lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, piglin)
                .create(LootContextParamSets.PIGLIN_BARTER);
        return lootTable.getRandomItems(lootParams);
    }

    private static int getExtraBarterRolls(int lootingLevel, ServerLevel level) {
        if (lootingLevel >= 5) {
            float roll = level.random.nextFloat();
            if (roll < 0.25F) {
                return 1;
            }
            return roll < 0.75F ? 2 : 3;
        }
        if (lootingLevel >= 3) {
            return level.random.nextBoolean() ? 1 : 2;
        }
        return 1;
    }
}
