package com.yukari.relicera.common.item;

import com.yukari.relicera.common.curio.FourfoldSherdPendantEffects;
import com.yukari.relicera.common.menu.FourfoldSherdPendantMenu;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class FourfoldSherdPendantItem extends Item implements ICurioItem {
    public static final int SHERD_SLOT_COUNT = 4;
    public static final String SHERDS_TAG = "Sherds";

    public FourfoldSherdPendantItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(
                    serverPlayer,
                    new SimpleMenuProvider(
                            (containerId, inventory, menuPlayer) -> new FourfoldSherdPendantMenu(containerId, inventory, stack),
                            Component.translatable("container.relicera.fourfold_sherd_pendant.title")
                    )
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        return FourfoldSherdPendantEffects.countInStack(stack, FourfoldSherdPendantEffects.SherdPattern.PRIZE);
    }

    @Override
    public int getLootingLevel(SlotContext slotContext, DamageSource source, LivingEntity target, int baseLooting, ItemStack stack) {
        return FourfoldSherdPendantEffects.countInStack(stack, FourfoldSherdPendantEffects.SherdPattern.SNORT);
    }
}
