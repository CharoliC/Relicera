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

public class AddOceanRuinArchaeologyLootModifier extends LootModifier {
    public static final Codec<AddOceanRuinArchaeologyLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddOceanRuinArchaeologyLootModifier::new));

    private static final ResourceLocation COLD_OCEAN_RUIN_ARCHAEOLOGY =
            ResourceLocation.withDefaultNamespace("archaeology/ocean_ruin_cold");
    private static final ResourceLocation WARM_OCEAN_RUIN_ARCHAEOLOGY =
            ResourceLocation.withDefaultNamespace("archaeology/ocean_ruin_warm");

    public AddOceanRuinArchaeologyLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        if (!COLD_OCEAN_RUIN_ARCHAEOLOGY.equals(lootTableId) && !WARM_OCEAN_RUIN_ARCHAEOLOGY.equals(lootTableId)) {
            return generatedLoot;
        }

        double driedCrownChance = ModCommonConfig.DRIED_CROWN_OCEAN_RUIN_ARCHAEOLOGY_CHANCE.get();
        double roll = context.getRandom().nextDouble();
        if (roll < driedCrownChance) {
            replaceArchaeologyLoot(generatedLoot, new ItemStack(ModItems.DRIED_CROWN.get()));
            return generatedLoot;
        }

        if (WARM_OCEAN_RUIN_ARCHAEOLOGY.equals(lootTableId)
                && roll < driedCrownChance + ModCommonConfig.RIPPLEHEART_PEARL_WARM_OCEAN_RUIN_ARCHAEOLOGY_CHANCE.get()) {
            replaceArchaeologyLoot(generatedLoot, new ItemStack(ModItems.RIPPLEHEART_PEARL.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_OCEAN_RUIN_ARCHAEOLOGY.get();
    }

    private static void replaceArchaeologyLoot(ObjectArrayList<ItemStack> generatedLoot, ItemStack stack) {
        generatedLoot.clear();
        generatedLoot.add(stack);
    }
}
