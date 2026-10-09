package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

public class BrutalPlunderBadgeItem extends QuickEquipCurioItem {
    public BrutalPlunderBadgeItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getLootingLevel(SlotContext slotContext, DamageSource source, LivingEntity target,
                              int baseLooting, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player) || slotContext.cosmetic()) {
            return 0;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity()).resolve()
                .map(handler -> handler.findFirstCurio(this)
                        .filter(result -> result.slotContext().identifier().equals(slotContext.identifier())
                                && result.slotContext().index() == slotContext.index())
                        .map(result -> ModCommonConfig.BRUTAL_PLUNDER_BADGE_LOOTING_BONUS.get())
                        .orElse(0))
                .orElse(0);
    }
}
