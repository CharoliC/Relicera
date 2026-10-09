package com.yukari.relicera.common.curio;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID)
public final class BaromsCovenantStoneTraderEffects {
    private static final String PERMANENT_TRADER_TAG = "ReliceraCovenantPermanentTrader";
    private static final String LAST_RESTOCK_DAY_TAG = "ReliceraCovenantLastRestockDay";

    private BaromsCovenantStoneTraderEffects() {
    }

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        if (!(event.getAbstractVillager() instanceof WanderingTrader trader)
                || !BaromsCovenantStoneEffects.hasContract(event.getEntity(), EntityType.WANDERING_TRADER)) {
            return;
        }
        CompoundTag data = trader.getPersistentData();
        if (!data.getBoolean(PERMANENT_TRADER_TAG)) {
            data.putBoolean(PERMANENT_TRADER_TAG, true);
            data.putLong(LAST_RESTOCK_DAY_TAG, currentDay(trader));
        }
        trader.setDespawnDelay(0);
    }

    @SubscribeEvent
    public static void onTraderTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof WanderingTrader trader)
                || !(trader.level() instanceof ServerLevel)
                || !trader.getPersistentData().getBoolean(PERMANENT_TRADER_TAG)) {
            return;
        }
        trader.setDespawnDelay(0);
        CompoundTag data = trader.getPersistentData();
        long day = currentDay(trader);
        if (day <= data.getLong(LAST_RESTOCK_DAY_TAG)) {
            return;
        }
        for (MerchantOffer offer : trader.getOffers()) {
            offer.resetUses();
        }
        data.putLong(LAST_RESTOCK_DAY_TAG, day);
    }

    private static long currentDay(WanderingTrader trader) {
        return trader.level().getServer().overworld().getDayTime() / 24000L;
    }
}
