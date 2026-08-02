package com.yukari.relicera.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class AddAstralStorybookLootModifier extends LootModifier {
    public static final Codec<AddAstralStorybookLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddAstralStorybookLootModifier::new));

    private static final Set<ResourceLocation> ADVANCED_CHEST_LOOT_TABLES = Set.of(
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"),
            ResourceLocation.withDefaultNamespace("chests/ancient_city"),
            ResourceLocation.withDefaultNamespace("chests/buried_treasure"),
            ResourceLocation.withDefaultNamespace("chests/end_city_treasure")
    );

    public AddAstralStorybookLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (ADVANCED_CHEST_LOOT_TABLES.contains(context.getQueriedLootTableId())
                && context.getRandom().nextDouble() < ModCommonConfig.ASTRAL_STORYBOOK_ADVANCED_CHEST_CHANCE.get()) {
            generatedLoot.add(new ItemStack(ModItems.ASTRAL_STORYBOOK.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_ASTRAL_STORYBOOK.get();
    }
}
