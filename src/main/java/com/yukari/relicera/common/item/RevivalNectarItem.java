package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModEffects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class RevivalNectarItem extends Item {
    private static final int DRINK_DURATION = 40;
    private static final int NUTRITION = 10;
    private static final float SATURATION_MODIFIER = 0.54F;
    private static final int ILUTHIAS_BLESSING_AMPLIFIER = 0;

    public RevivalNectarItem(Properties properties) {
        super(properties);
    }

    public static FoodProperties createFoodProperties() {
        return new FoodProperties.Builder()
                .nutrition(NUTRITION)
                .saturationMod(SATURATION_MODIFIER)
                .alwaysEat()
                .effect(RevivalNectarItem::createIluthiasBlessingEffect, 1.0F)
                .build();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);

        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        PotionUtils.addPotionTooltip(List.of(createIluthiasBlessingEffect()), tooltip, 1.0F);
    }

    private static MobEffectInstance createIluthiasBlessingEffect() {
        return new MobEffectInstance(
                ModEffects.ILUTHIAS_BLESSING.get(),
                ModCommonConfig.REVIVAL_NECTAR_ILUTHIAS_BLESSING_DURATION.get(),
                ILUTHIAS_BLESSING_AMPLIFIER
        );
    }
}
