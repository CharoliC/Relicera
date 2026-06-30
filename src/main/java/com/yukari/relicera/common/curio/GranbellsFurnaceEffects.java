package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModServerConfig;
import com.yukari.relicera.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public final class GranbellsFurnaceEffects {
    private static final int RETALIATION_FIRE_SECONDS = 15;
    private static final double LAVA_SURFACE_UPWARD_SPEED = 0.07D;
    private static final double LAVA_CROUCH_DOWNWARD_SPEED = -0.01D;

    private static final Map<UUID, SmithingSnapshot> SMITHING_SNAPSHOTS = new HashMap<>();
    private static final Map<UUID, InventorySnapshot> KEPT_INVENTORIES = new HashMap<>();

    private GranbellsFurnaceEffects() {
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.GRANBELLS_FURNACE.get()))
                .orElse(false);
    }

    public static void tickPlayer(Player player) {
        if (!player.isAlive() || !isEquipped(player)) {
            return;
        }

        player.clearFire();

        if (player instanceof ServerPlayer serverPlayer) {
            preserveSmithingTemplate(serverPlayer);
        }
    }

    public static boolean preventFireDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isEquipped(player) && isFireOrLavaDamage(event.getSource())) {
            player.clearFire();
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public static boolean preventFireDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && isEquipped(player) && isFireOrLavaDamage(event.getSource())) {
            player.clearFire();
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public static void applyOutgoingDamageBonus(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !isEquipped(player)) {
            return;
        }

        double damageBonus = ModServerConfig.GRANBELLS_FURNACE_DAMAGE_BONUS.get();
        if (damageBonus > 0.0D) {
            event.setAmount(event.getAmount() + (float) damageBonus);
        }
    }

    public static void igniteAttacker(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isEquipped(player)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity livingAttacker && livingAttacker != player) {
            livingAttacker.setSecondsOnFire(RETALIATION_FIRE_SECONDS);
        }
    }

    public static void preventAnvilDamage(AnvilRepairEvent event) {
        if (isEquipped(event.getEntity())) {
            event.setBreakChance(0.0F);
        }
    }

    public static void tickLavaStanding(LivingEntity entity) {
        if (entity instanceof Player player && isEquipped(player)) {
            standOnLava(player);
        }
    }

    private static void standOnLava(Player player) {
        if (!player.isInLava() || !player.isAffectedByFluids()) {
            return;
        }

        CollisionContext collisionContext = CollisionContext.of(player);
        BlockPos pos = player.blockPosition();
        if (collisionContext.isAbove(LiquidBlock.STABLE_SHAPE, pos, true)
                && !player.level().getFluidState(pos.above()).is(FluidTags.LAVA)) {
            player.setOnGround(true);
        } else {
            player.setDeltaMovement(player.getDeltaMovement().add(
                    0.0D,
                    player.isCrouching() ? LAVA_CROUCH_DOWNWARD_SPEED : LAVA_SURFACE_UPWARD_SPEED,
                    0.0D
            ));
        }
    }

    private static boolean isFireOrLavaDamage(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR);
    }

    public static void rememberInventoryForFireOrLavaDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !shouldKeepInventoryForDeath(player, event.getSource())) {
            return;
        }

        KEPT_INVENTORIES.put(player.getUUID(), InventorySnapshot.capture(player));
    }

    public static boolean cancelKeptInventoryDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player && KEPT_INVENTORIES.containsKey(player.getUUID())) {
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public static void keepCuriosForFireOrLavaDeath(DropRulesEvent event) {
        if (event.getEntity() instanceof Player player && KEPT_INVENTORIES.containsKey(player.getUUID())) {
            event.addOverride(stack -> true, ICurio.DropRule.ALWAYS_KEEP);
        }
    }

    public static void restoreKeptInventory(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        InventorySnapshot snapshot = KEPT_INVENTORIES.remove(event.getOriginal().getUUID());
        if (snapshot == null) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            snapshot.restore(player);
        }
    }

    private static boolean shouldKeepInventoryForDeath(Player player, DamageSource source) {
        return ModServerConfig.GRANBELLS_FURNACE_KEEP_INVENTORY_IN_FIRE_OR_LAVA.get()
                && isEquipped(player)
                && (isInFireOrLava(player) || isFireOrLavaDamage(source));
    }

    private static boolean isInFireOrLava(Player player) {
        if (player.isInLava() || player.isOnFire()) {
            return true;
        }

        BlockPos feet = player.blockPosition();
        return isFireOrLavaBlock(player, feet)
                || isFireOrLavaBlock(player, feet.above())
                || isMagmaBlock(player, feet)
                || isMagmaBlock(player, feet.below());
    }

    private static boolean isFireOrLavaBlock(Player player, BlockPos pos) {
        return player.level().getBlockState(pos).is(BlockTags.FIRE)
                || player.level().getFluidState(pos).is(FluidTags.LAVA);
    }

    private static boolean isMagmaBlock(Player player, BlockPos pos) {
        return player.level().getBlockState(pos).is(Blocks.MAGMA_BLOCK);
    }

    private static void preserveSmithingTemplate(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!(player.containerMenu instanceof SmithingMenu smithingMenu)
                || !ModServerConfig.GRANBELLS_FURNACE_PRESERVE_SMITHING_TEMPLATES.get()
                || !isEquipped(player)) {
            SMITHING_SNAPSHOTS.remove(playerId);
            return;
        }

        SmithingSnapshot previous = SMITHING_SNAPSHOTS.get(playerId);
        SmithingSnapshot current = SmithingSnapshot.capture(smithingMenu);
        if (previous != null && shouldRestoreTemplate(previous, current)) {
            restoreTemplate(smithingMenu, previous.template());
            current = SmithingSnapshot.capture(smithingMenu);
        }

        SMITHING_SNAPSHOTS.put(playerId, current);
    }

    private static boolean shouldRestoreTemplate(SmithingSnapshot previous, SmithingSnapshot current) {
        return previous.hasResult()
                && decreasedByOne(previous.template(), current.template())
                && decreasedByOne(previous.base(), current.base())
                && decreasedByOne(previous.addition(), current.addition());
    }

    private static boolean decreasedByOne(ItemStack previous, ItemStack current) {
        if (previous.isEmpty()) {
            return false;
        }

        if (previous.getCount() == 1 && current.isEmpty()) {
            return true;
        }

        return !current.isEmpty()
                && ItemStack.isSameItemSameTags(previous, current)
                && current.getCount() == previous.getCount() - 1;
    }

    private static void restoreTemplate(SmithingMenu smithingMenu, ItemStack previousTemplate) {
        Slot templateSlot = smithingMenu.getSlot(SmithingMenu.TEMPLATE_SLOT);
        ItemStack currentTemplate = templateSlot.getItem();
        if (currentTemplate.isEmpty()) {
            templateSlot.set(previousTemplate.copyWithCount(1));
        } else if (ItemStack.isSameItemSameTags(previousTemplate, currentTemplate)) {
            currentTemplate.grow(1);
            templateSlot.set(currentTemplate);
        }
        smithingMenu.broadcastChanges();
    }

    private record SmithingSnapshot(ItemStack template, ItemStack base, ItemStack addition, boolean hasResult) {
        private static SmithingSnapshot capture(SmithingMenu smithingMenu) {
            return new SmithingSnapshot(
                    smithingMenu.getSlot(SmithingMenu.TEMPLATE_SLOT).getItem().copy(),
                    smithingMenu.getSlot(SmithingMenu.BASE_SLOT).getItem().copy(),
                    smithingMenu.getSlot(SmithingMenu.ADDITIONAL_SLOT).getItem().copy(),
                    !smithingMenu.getSlot(SmithingMenu.RESULT_SLOT).getItem().isEmpty()
            );
        }
    }

    private record InventorySnapshot(ListTag items, int selectedSlot) {
        private static InventorySnapshot capture(ServerPlayer player) {
            Inventory inventory = player.getInventory();
            return new InventorySnapshot(inventory.save(new ListTag()), inventory.selected);
        }

        private void restore(ServerPlayer player) {
            Inventory inventory = player.getInventory();
            inventory.load(items.copy());
            inventory.selected = selectedSlot;
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
        }
    }
}
