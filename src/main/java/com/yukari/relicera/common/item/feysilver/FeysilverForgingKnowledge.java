package com.yukari.relicera.common.item.feysilver;

import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.SyncFeysilverForgingKnowledgePacket;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class FeysilverForgingKnowledge {
    private static final String DATA_KEY = "ReliceraFeysilverForging";
    private static final String VOLUME_ONE_KEY = "VolumeOne";

    private FeysilverForgingKnowledge() {
    }

    public static boolean hasVolumeOne(Player player) {
        return player.getPersistentData().getCompound(DATA_KEY).getBoolean(VOLUME_ONE_KEY);
    }

    public static void learnVolumeOne(Player player) {
        getOrCreateData(player).putBoolean(VOLUME_ONE_KEY, true);
        if (player instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }

    public static void copy(Player original, Player replacement) {
        if (hasVolumeOne(original)) {
            learnVolumeOne(replacement);
        }
    }

    public static void sync(ServerPlayer player) {
        ModNetworking.sendToPlayer(new SyncFeysilverForgingKnowledgePacket(hasVolumeOne(player)), player);
    }

    public static void updateAnvilResult(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (player == null || !hasVolumeOne(player)) {
            return;
        }

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!right.is(ModItems.FEYSILVER_INGOT.get()) || !canForge(left)) {
            return;
        }

        ItemStack output = left.copy();
        output.setDamageValue(0);
        FeysilverCoating.apply(output);
        event.setOutput(output);
        event.setCost(ModCommonConfig.FEYSILVER_FORGING_ART_VOLUME_ONE_EXPERIENCE_LEVEL_COST.get());
        event.setMaterialCost(1);
    }

    private static boolean canForge(ItemStack stack) {
        return !stack.isEmpty()
                && stack.isDamageableItem()
                && !FeysilverCoating.isUnbreakable(stack)
                && !isBlacklisted(stack);
    }

    private static boolean isBlacklisted(ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null
                && ModCommonConfig.FEYSILVER_FORGING_ART_VOLUME_ONE_ITEM_BLACKLIST.get().contains(itemId.toString());
    }

    private static CompoundTag getOrCreateData(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            root.put(DATA_KEY, new CompoundTag());
        }
        return root.getCompound(DATA_KEY);
    }
}
