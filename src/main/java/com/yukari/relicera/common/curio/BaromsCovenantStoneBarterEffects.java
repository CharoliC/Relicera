package com.yukari.relicera.common.curio;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID)
public final class BaromsCovenantStoneBarterEffects {
    private static final int BARTER_DELAY_TICKS = 40;
    private static final String PENDING_BARTER_TAG = "ReliceraCovenantPendingBarter";

    private BaromsCovenantStoneBarterEffects() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        startBarter(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        startBarter(event);
    }

    private static void startBarter(PlayerInteractEvent event) {
        Player player = event.getEntity();
        ItemStack gold = player.getItemInHand(event.getHand());
        if (!player.isShiftKeyDown() || !gold.is(Items.GOLD_INGOT)
                || !BaromsCovenantStoneEffects.hasContract(player, EntityType.PIGLIN)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
        if (!(player instanceof ServerPlayer serverPlayer)
                || player.getPersistentData().contains(PENDING_BARTER_TAG)
                || player.getCooldowns().isOnCooldown(Items.GOLD_INGOT)) {
            return;
        }

        Piglin piglin = EntityType.PIGLIN.create(serverPlayer.serverLevel());
        if (piglin == null) {
            return;
        }
        piglin.setPos(player.position());
        LootParams params = new LootParams.Builder(serverPlayer.serverLevel())
                .withParameter(LootContextParams.THIS_ENTITY, piglin)
                .create(LootContextParamSets.PIGLIN_BARTER);
        ListTag results = new ListTag();
        serverPlayer.server.getLootData().getLootTable(BuiltInLootTables.PIGLIN_BARTERING)
                .getRandomItems(params).stream().filter(stack -> !stack.isEmpty())
                .forEach(stack -> results.add(stack.save(new CompoundTag())));

        CompoundTag pending = new CompoundTag();
        pending.put("Items", results);
        pending.putInt("Ticks", BARTER_DELAY_TICKS);
        player.getPersistentData().put(PENDING_BARTER_TAG, pending);
        gold.shrink(1);
        player.getCooldowns().addCooldown(Items.GOLD_INGOT, BARTER_DELAY_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PIGLIN_ADMIRING_ITEM,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        serverPlayer.inventoryMenu.broadcastChanges();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || !player.getPersistentData().contains(PENDING_BARTER_TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag pending = player.getPersistentData().getCompound(PENDING_BARTER_TAG);
        int remaining = pending.getInt("Ticks") - 1;
        if (remaining > 0) {
            pending.putInt("Ticks", remaining);
            return;
        }
        ListTag results = pending.getList("Items", Tag.TAG_COMPOUND);
        player.getPersistentData().remove(PENDING_BARTER_TAG);
        for (int index = 0; index < results.size(); index++) {
            ItemStack result = ItemStack.of(results.getCompound(index));
            if (!result.isEmpty()) {
                player.drop(result, false);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        if (original.contains(PENDING_BARTER_TAG, Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put(PENDING_BARTER_TAG,
                    original.getCompound(PENDING_BARTER_TAG).copy());
            original.remove(PENDING_BARTER_TAG);
        }
    }
}
