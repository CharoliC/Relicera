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

import java.util.Set;

public class AddVindicatorsMedalLootModifier extends LootModifier {
    public static final Codec<AddVindicatorsMedalLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddVindicatorsMedalLootModifier::new));

    private static final ResourceLocation VINDICATOR_LOOT_TABLE =
            ResourceLocation.withDefaultNamespace("entities/vindicator");
    private static final Set<ResourceLocation> STRUCTURE_CHEST_LOOT_TABLES = Set.of(
            ResourceLocation.withDefaultNamespace("chests/pillager_outpost"),
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion")
    );

    public AddVindicatorsMedalLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        double chance;
        if (VINDICATOR_LOOT_TABLE.equals(lootTableId)) {
            chance = ModCommonConfig.VINDICATORS_MEDAL_VINDICATOR_DROP_CHANCE.get();
        } else if (STRUCTURE_CHEST_LOOT_TABLES.contains(lootTableId)) {
            chance = ModCommonConfig.VINDICATORS_MEDAL_STRUCTURE_CHEST_CHANCE.get();
        } else {
            return generatedLoot;
        }

        if (context.getRandom().nextDouble() < chance) {
            generatedLoot.add(new ItemStack(ModItems.VINDICATORS_MEDAL.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_VINDICATORS_MEDAL.get();
    }
}
