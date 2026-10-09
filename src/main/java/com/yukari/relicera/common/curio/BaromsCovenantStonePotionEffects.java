package com.yukari.relicera.common.curio;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID)
public final class BaromsCovenantStonePotionEffects {
    private BaromsCovenantStonePotionEffects() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand());
        if (!(held.getItem() instanceof ThrowablePotionItem) || player.getAbilities().instabuild
                || player.getCooldowns().isOnCooldown(held.getItem())
                || !BaromsCovenantStoneEffects.hasContract(player, EntityType.WITCH)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack original = held.copy();
        InteractionResultHolder<ItemStack> useResult = held.use(player.level(), player, event.getHand());
        ItemStack result = useResult.getObject();
        if (useResult.getResult().consumesAction() && consumedOne(original, result) && shouldPreserve(player)) {
            result = original;
        }
        player.setItemInHand(event.getHand(), result.isEmpty() ? ItemStack.EMPTY : result);
        serverPlayer.inventoryMenu.sendAllDataToRemote();
        event.setCancellationResult(useResult.getResult());
    }

    private static boolean consumedOne(ItemStack original, ItemStack result) {
        return original.getCount() == 1 && result.isEmpty()
                || original.getCount() > 1 && result.getCount() == original.getCount() - 1
                && ItemStack.isSameItemSameTags(original, result);
    }

    private static boolean shouldPreserve(LivingEntity wearer) {
        return wearer.getRandom().nextDouble() < ModCommonConfig.BAROM_WITCH_POTION_PRESERVATION_CHANCE.get();
    }
}
