package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class RippleheartPearlDrops {
    private RippleheartPearlDrops() {
    }

    public static void tryDropFromAxolotlAssist(Axolotl axolotl, Player player) {
        if (!(axolotl.level() instanceof ServerLevel level)
                || level.random.nextDouble() >= ModCommonConfig.RIPPLEHEART_PEARL_AXOLOTL_ASSIST_CHANCE.get()) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(
                level,
                player.getX(),
                player.getY() + 0.5D,
                player.getZ(),
                new ItemStack(ModItems.RIPPLEHEART_PEARL.get())
        );
        itemEntity.setDefaultPickUpDelay();
        itemEntity.setGlowingTag(true);
        level.addFreshEntity(itemEntity);
    }
}
