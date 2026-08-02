package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class AstralStorybookItem extends Item {
    private static final String DATA_KEY = "AstralStorybook";
    private static final String ENCHANTMENT_KEY = "Enchantment";
    private static final String LEVEL_KEY = "Level";

    public AstralStorybookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        int experiencePointCost = ModCommonConfig.ASTRAL_STORYBOOK_EXPERIENCE_POINT_COST.get();
        if (!player.getAbilities().instabuild && getCurrentExperiencePoints(player) < experiencePointCost) {
            return InteractionResultHolder.fail(stack);
        }

        List<Enchantment> candidates = getRerollCandidates(stack);
        if (candidates.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        Enchantment enchantment = candidates.get(player.getRandom().nextInt(candidates.size()));
        int enchantmentLevel = rollLevel(enchantment, player);
        setRecordedEnchantment(stack, enchantment, enchantmentLevel);
        if (!player.getAbilities().instabuild) {
            player.giveExperiencePoints(-experiencePointCost);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverPlayer.displayClientMessage(getEnchantmentDisplayName(enchantment, enchantmentLevel), true);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack storybook, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || slot.getItem().isEmpty()) {
            return false;
        }

        Optional<RecordedEnchantment> recorded = getRecordedEnchantment(storybook);
        if (recorded.isEmpty()) {
            return true;
        }

        ItemStack target = slot.getItem();
        RecordedEnchantment entry = recorded.get();
        if (!canApplyTo(entry.enchantment(), target)) {
            return true;
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(target);
        if (enchantments.getOrDefault(entry.enchantment(), 0) >= entry.level()) {
            return true;
        }

        ItemStack enchantedTarget = createEnchantedTarget(target, entry, enchantments);
        boolean stackedBooks = target.is(Items.BOOK) && target.getCount() > 1;
        ItemStack slotReplacement = stackedBooks ? createRemainingBooks(target) : enchantedTarget;
        if (enchantedTarget.isEmpty()
                || !slot.mayPickup(player)
                || !slot.mayPlace(slotReplacement)) {
            return true;
        }

        if (!player.level().isClientSide) {
            slot.set(slotReplacement);
            slot.setChanged();
            if (stackedBooks) {
                giveOrDrop(player, enchantedTarget);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    public static Optional<RecordedEnchantment> getRecordedEnchantment(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        CompoundTag data = root.getCompound(DATA_KEY);
        ResourceLocation enchantmentId = ResourceLocation.tryParse(data.getString(ENCHANTMENT_KEY));
        Enchantment enchantment = enchantmentId == null ? null : ForgeRegistries.ENCHANTMENTS.getValue(enchantmentId);
        int level = data.getInt(LEVEL_KEY);
        return enchantment == null || level < 1
                ? Optional.empty()
                : Optional.of(new RecordedEnchantment(enchantment, level));
    }

    public static Component getEnchantmentDisplayName(RecordedEnchantment recorded) {
        return getEnchantmentDisplayName(recorded.enchantment(), recorded.level());
    }

    private static Component getEnchantmentDisplayName(Enchantment enchantment, int level) {
        ChatFormatting color;
        if (enchantment.isCurse()) {
            color = ChatFormatting.DARK_RED;
        } else if (enchantment.isTreasureOnly()) {
            color = ChatFormatting.GOLD;
        } else {
            color = ChatFormatting.GRAY;
        }
        return enchantment.getFullname(level).copy().withStyle(color);
    }

    private static List<Enchantment> getRerollCandidates(ItemStack stack) {
        Enchantment current = getRecordedEnchantment(stack)
                .map(RecordedEnchantment::enchantment)
                .orElse(null);
        List<Enchantment> candidates = new ArrayList<>();
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            if (enchantment != current) {
                candidates.add(enchantment);
            }
        }
        return candidates;
    }

    private static long getCurrentExperiencePoints(Player player) {
        long level = Math.max(player.experienceLevel, 0);
        if (level >= 21864L) {
            return Integer.MAX_VALUE;
        }

        long completedLevelPoints;
        if (level <= 16L) {
            completedLevelPoints = level * level + 6L * level;
        } else if (level <= 31L) {
            completedLevelPoints = (5L * level * level - 81L * level + 720L) / 2L;
        } else {
            completedLevelPoints = (9L * level * level - 325L * level + 4440L) / 2L;
        }

        long progressPoints = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return completedLevelPoints + Math.max(progressPoints, 0L);
    }

    private static int rollLevel(Enchantment enchantment, Player player) {
        int maxLevel = enchantment.getMaxLevel();
        if (maxLevel < 2) {
            return 1;
        }
        return maxLevel < Integer.MAX_VALUE
                && player.getRandom().nextDouble() < ModCommonConfig.ASTRAL_STORYBOOK_ABOVE_MAX_LEVEL_CHANCE.get()
                ? maxLevel + 1
                : maxLevel;
    }

    private static void setRecordedEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        ResourceLocation enchantmentId = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (enchantmentId == null) {
            return;
        }

        CompoundTag data = new CompoundTag();
        data.putString(ENCHANTMENT_KEY, enchantmentId.toString());
        data.putInt(LEVEL_KEY, level);
        stack.getOrCreateTag().put(DATA_KEY, data);
    }

    private static boolean canApplyTo(Enchantment enchantment, ItemStack target) {
        return target.is(Items.BOOK)
                || target.is(Items.ENCHANTED_BOOK)
                || enchantment.canEnchant(target);
    }

    private static ItemStack createEnchantedTarget(ItemStack target, RecordedEnchantment recorded,
                                                   Map<Enchantment, Integer> enchantments) {
        ItemStack result;
        if (target.is(Items.BOOK)) {
            result = new ItemStack(Items.ENCHANTED_BOOK);
            if (target.hasTag()) {
                result.setTag(target.getTag().copy());
                result.removeTagKey("Enchantments");
            }
            result.setCount(1);
        } else {
            result = target.copy();
        }

        enchantments.put(recorded.enchantment(), recorded.level());
        EnchantmentHelper.setEnchantments(enchantments, result);
        return result;
    }

    private static ItemStack createRemainingBooks(ItemStack originalTarget) {
        ItemStack remainder = originalTarget.copy();
        remainder.shrink(1);
        return remainder;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    public record RecordedEnchantment(Enchantment enchantment, int level) {
    }
}
