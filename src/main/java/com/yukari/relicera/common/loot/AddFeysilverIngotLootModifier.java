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

public class AddFeysilverIngotLootModifier extends LootModifier {
    public static final Codec<AddFeysilverIngotLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddFeysilverIngotLootModifier::new));

    private static final Set<ResourceLocation> OVERWORLD_CHEST_LOOT_TABLES = Set.of(
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
            ResourceLocation.withDefaultNamespace("chests/stronghold_crossing"),
            ResourceLocation.withDefaultNamespace("chests/stronghold_corridor"),
            ResourceLocation.withDefaultNamespace("chests/desert_pyramid"),
            ResourceLocation.withDefaultNamespace("chests/jungle_temple"),
            ResourceLocation.withDefaultNamespace("chests/igloo_chest"),
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"),
            ResourceLocation.withDefaultNamespace("chests/shipwreck_supply")
    );

    public AddFeysilverIngotLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (OVERWORLD_CHEST_LOOT_TABLES.contains(context.getQueriedLootTableId())
                && context.getRandom().nextDouble() < ModCommonConfig.FEYSILVER_INGOT_OVERWORLD_CHEST_CHANCE.get()) {
            generatedLoot.add(new ItemStack(ModItems.FEYSILVER_INGOT.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_FEYSILVER_INGOT.get();
    }
}
