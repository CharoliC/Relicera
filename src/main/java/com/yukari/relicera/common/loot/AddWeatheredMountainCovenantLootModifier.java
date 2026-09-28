package com.yukari.relicera.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class AddWeatheredMountainCovenantLootModifier extends LootModifier {
    public static final Codec<AddWeatheredMountainCovenantLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddWeatheredMountainCovenantLootModifier::new));

    private static final ResourceLocation TRAIL_RUINS_COMMON =
            ResourceLocation.withDefaultNamespace("archaeology/trail_ruins_common");
    private static final ResourceLocation TRAIL_RUINS_RARE =
            ResourceLocation.withDefaultNamespace("archaeology/trail_ruins_rare");
    private static final ResourceLocation PILLAGER_OUTPOST =
            ResourceLocation.withDefaultNamespace("chests/pillager_outpost");
    private static final ResourceLocation ABANDONED_MINESHAFT =
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft");
    private static final ResourceLocation SIMPLE_DUNGEON =
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon");

    public AddWeatheredMountainCovenantLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        double chance = getChance(lootTableId);
        if (chance <= 0.0D || context.getRandom().nextDouble() >= chance) {
            return generatedLoot;
        }

        if (TRAIL_RUINS_COMMON.equals(lootTableId) || TRAIL_RUINS_RARE.equals(lootTableId)) {
            generatedLoot.clear();
        }
        generatedLoot.add(new ItemStack(ModItems.WEATHERED_MOUNTAIN_COVENANT.get()));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_WEATHERED_MOUNTAIN_COVENANT.get();
    }

    private static double getChance(ResourceLocation lootTableId) {
        if (TRAIL_RUINS_COMMON.equals(lootTableId) || TRAIL_RUINS_RARE.equals(lootTableId)) {
            return ModCommonConfig.WEATHERED_MOUNTAIN_COVENANT_TRAIL_RUINS_ARCHAEOLOGY_CHANCE.get();
        }
        if (PILLAGER_OUTPOST.equals(lootTableId)) {
            return ModCommonConfig.WEATHERED_MOUNTAIN_COVENANT_PILLAGER_OUTPOST_CHEST_CHANCE.get();
        }
        if (ABANDONED_MINESHAFT.equals(lootTableId) || SIMPLE_DUNGEON.equals(lootTableId)) {
            return ModCommonConfig.WEATHERED_MOUNTAIN_COVENANT_MINESHAFT_DUNGEON_CHEST_CHANCE.get();
        }
        return 0.0D;
    }
}
