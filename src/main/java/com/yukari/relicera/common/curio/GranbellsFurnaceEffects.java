package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
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
    private static final String SOPHISTICATED_BACKPACKS_SMITHING_CONTAINER =
            "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.smithing.SmithingUpgradeContainer";
    private static final int RETALIATION_FIRE_SECONDS = 15;
    private static final double LAVA_SURFACE_UPWARD_SPEED = 0.07D;
    private static final double LAVA_CROUCH_DOWNWARD_SPEED = -0.01D;

    private static final Map<UUID, SmithingSnapshot> SMITHING_SNAPSHOTS = new HashMap<>();
    private static final Map<Class<?>, Optional<ExternalSmithingAccess>> EXTERNAL_SMITHING_ACCESS = new HashMap<>();
    private static final Map<Class<?>, Optional<ExternalStorageMenuAccess>> EXTERNAL_STORAGE_MENU_ACCESS = new HashMap<>();
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

        double damageBonus = ModCommonConfig.GRANBELLS_FURNACE_DAMAGE_BONUS.get();
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
        return ModCommonConfig.GRANBELLS_FURNACE_KEEP_INVENTORY_IN_FIRE_OR_LAVA.get()
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
        SmithingSlots smithingSlots = getSmithingSlots(player.containerMenu);
        if (smithingSlots == null
                || !ModCommonConfig.GRANBELLS_FURNACE_PRESERVE_SMITHING_TEMPLATES.get()
                || !isEquipped(player)) {
            SMITHING_SNAPSHOTS.remove(playerId);
            return;
        }

        SmithingSnapshot previous = SMITHING_SNAPSHOTS.get(playerId);
        SmithingSnapshot current = SmithingSnapshot.capture(smithingSlots);
        if (previous != null && shouldRestoreTemplate(previous, current)) {
            restoreTemplate(smithingSlots, previous.template());
            player.containerMenu.broadcastChanges();
            current = SmithingSnapshot.capture(smithingSlots);
        }

        SMITHING_SNAPSHOTS.put(playerId, current);
    }

    private static SmithingSlots getSmithingSlots(AbstractContainerMenu menu) {
        if (menu instanceof SmithingMenu smithingMenu) {
            return new SmithingSlots(
                    smithingMenu.getSlot(SmithingMenu.TEMPLATE_SLOT),
                    smithingMenu.getSlot(SmithingMenu.BASE_SLOT),
                    smithingMenu.getSlot(SmithingMenu.ADDITIONAL_SLOT),
                    smithingMenu.getSlot(SmithingMenu.RESULT_SLOT)
            );
        }

        SmithingSlots externalSmithingSlots = getExternalSmithingSlots(menu);
        return externalSmithingSlots != null ? externalSmithingSlots : getExternalStorageSmithingSlots(menu);
    }

    private static SmithingSlots getExternalSmithingSlots(Object menu) {
        if (!SOPHISTICATED_BACKPACKS_SMITHING_CONTAINER.equals(menu.getClass().getName())) {
            return null;
        }

        return EXTERNAL_SMITHING_ACCESS
                .computeIfAbsent(menu.getClass(), GranbellsFurnaceEffects::createExternalSmithingAccess)
                .map(access -> access.getSlots(menu))
                .orElse(null);
    }

    private static SmithingSlots getExternalStorageSmithingSlots(AbstractContainerMenu menu) {
        return EXTERNAL_STORAGE_MENU_ACCESS
                .computeIfAbsent(menu.getClass(), GranbellsFurnaceEffects::createExternalStorageMenuAccess)
                .map(access -> access.findSmithingSlots(menu))
                .orElse(null);
    }

    private static Optional<ExternalSmithingAccess> createExternalSmithingAccess(Class<?> menuClass) {
        try {
            return Optional.of(new ExternalSmithingAccess(
                    menuClass.getMethod("getTemplateSlot"),
                    menuClass.getMethod("getBaseSlot"),
                    menuClass.getMethod("getAdditionalSlot"),
                    menuClass.getMethod("getResultSlot")
            ));
        } catch (NoSuchMethodException exception) {
            return Optional.empty();
        }
    }

    private static Optional<ExternalStorageMenuAccess> createExternalStorageMenuAccess(Class<?> menuClass) {
        try {
            return Optional.of(new ExternalStorageMenuAccess(menuClass.getMethod("getUpgradeContainers")));
        } catch (NoSuchMethodException exception) {
            return Optional.empty();
        }
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

    private static void restoreTemplate(SmithingSlots smithingSlots, ItemStack previousTemplate) {
        Slot templateSlot = smithingSlots.template();
        ItemStack currentTemplate = templateSlot.getItem();
        if (currentTemplate.isEmpty()) {
            templateSlot.set(previousTemplate.copyWithCount(1));
        } else if (ItemStack.isSameItemSameTags(previousTemplate, currentTemplate)) {
            currentTemplate.grow(1);
            templateSlot.set(currentTemplate);
        }
        templateSlot.setChanged();
    }

    private record SmithingSlots(Slot template, Slot base, Slot addition, Slot result) {
    }

    private record SmithingSnapshot(ItemStack template, ItemStack base, ItemStack addition, boolean hasResult) {
        private static SmithingSnapshot capture(SmithingSlots smithingSlots) {
            return new SmithingSnapshot(
                    smithingSlots.template().getItem().copy(),
                    smithingSlots.base().getItem().copy(),
                    smithingSlots.addition().getItem().copy(),
                    !smithingSlots.result().getItem().isEmpty()
            );
        }
    }

    private record ExternalSmithingAccess(Method getTemplateSlot, Method getBaseSlot, Method getAdditionalSlot, Method getResultSlot) {
        private SmithingSlots getSlots(Object menu) {
            try {
                Slot template = getSlot(menu, getTemplateSlot);
                Slot base = getSlot(menu, getBaseSlot);
                Slot addition = getSlot(menu, getAdditionalSlot);
                Slot result = getSlot(menu, getResultSlot);
                if (template == null || base == null || addition == null || result == null) {
                    return null;
                }
                return new SmithingSlots(template, base, addition, result);
            } catch (ReflectiveOperationException exception) {
                return null;
            }
        }

        private static Slot getSlot(Object menu, Method getter) throws ReflectiveOperationException {
            Object value = getter.invoke(menu);
            return value instanceof Slot slot ? slot : null;
        }
    }

    private record ExternalStorageMenuAccess(Method getUpgradeContainers) {
        private SmithingSlots findSmithingSlots(Object menu) {
            try {
                Object value = getUpgradeContainers.invoke(menu);
                if (!(value instanceof Map<?, ?> upgradeContainers)) {
                    return null;
                }

                for (Object upgradeContainer : upgradeContainers.values()) {
                    if (upgradeContainer == null) {
                        continue;
                    }

                    SmithingSlots smithingSlots = getExternalSmithingSlots(upgradeContainer);
                    if (smithingSlots != null) {
                        return smithingSlots;
                    }
                }
            } catch (ReflectiveOperationException exception) {
                return null;
            }

            return null;
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
