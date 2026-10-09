package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModSoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class RabbitsPocketWatchItem extends Item {
    public RabbitsPocketWatchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack watch = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            ForgeRegistries.ITEMS.getValues().stream()
                    .filter(player.getCooldowns()::isOnCooldown)
                    .forEach(player.getCooldowns()::removeCooldown);

            int cooldownTicks = ModCommonConfig.RABBITS_POCKET_WATCH_COOLDOWN_TICKS.get();
            if (cooldownTicks > 0) {
                player.getCooldowns().addCooldown(this, cooldownTicks);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSoundEvents.RABBITS_POCKET_WATCH_USE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
        return InteractionResultHolder.sidedSuccess(watch, level.isClientSide());
    }
}
