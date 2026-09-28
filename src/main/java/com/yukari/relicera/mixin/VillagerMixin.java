package com.yukari.relicera.mixin;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.curio.CovenantTabletEffects;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    private static final String HEART_OF_THE_MOUNTAIN_TRADE_ROLLED_KEY =
            ReliceraMod.MOD_ID + ":heart_of_the_mountain_trade_rolled";

    @Inject(method = "updateSpecialPrices", at = @At("RETURN"))
    private void relicera$applyCovenantTabletVillagerDiscount(Player player, CallbackInfo ci) {
        double discount = ModCommonConfig.COVENANT_TABLET_VILLAGER_TRADE_DISCOUNT.get();
        Villager villager = (Villager) (Object) this;
        if (discount > 0.0D && CovenantTabletEffects.hasVillagerDiscount(player)) {
            for (MerchantOffer offer : villager.getOffers()) {
                int priceReduction = Mth.floor(discount * offer.getBaseCostA().getCount());
                offer.addToSpecialPriceDiff(-Math.max(priceReduction, 1));
            }
        }
    }

    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void relicera$addHeartOfTheMountainTrade(CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (villager.getVillagerData().getProfession() != VillagerProfession.MASON
                || villager.getVillagerData().getLevel() != 5
                || villager.getPersistentData().getBoolean(HEART_OF_THE_MOUNTAIN_TRADE_ROLLED_KEY)) {
            return;
        }

        villager.getPersistentData().putBoolean(HEART_OF_THE_MOUNTAIN_TRADE_ROLLED_KEY, true);
        if (villager.getRandom().nextFloat() >= 0.22F) {
            return;
        }

        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 28),
                new ItemStack(Items.ANCIENT_DEBRIS, 3),
                new ItemStack(ModItems.HEART_OF_THE_MOUNTAIN.get()),
                1,
                30,
                0.2F
        ));
    }
}
