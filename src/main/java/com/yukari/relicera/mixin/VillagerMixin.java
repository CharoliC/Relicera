package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.CovenantTabletEffects;
import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    @Inject(method = "updateSpecialPrices", at = @At("RETURN"))
    private void relicera$applyCovenantTabletVillagerDiscount(Player player, CallbackInfo ci) {
        if (!CovenantTabletEffects.hasVillagerDiscount(player)) {
            return;
        }

        double discount = ModCommonConfig.COVENANT_TABLET_VILLAGER_TRADE_DISCOUNT.get();
        if (discount <= 0.0D) {
            return;
        }

        Villager villager = (Villager) (Object) this;
        for (MerchantOffer offer : villager.getOffers()) {
            int priceReduction = Mth.floor(discount * offer.getBaseCostA().getCount());
            offer.addToSpecialPriceDiff(-Math.max(priceReduction, 1));
        }
    }
}
