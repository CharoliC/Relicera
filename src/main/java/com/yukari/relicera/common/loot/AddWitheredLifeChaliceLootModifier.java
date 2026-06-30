package com.yukari.relicera.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yukari.relicera.config.ModServerConfig;
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

public class AddWitheredLifeChaliceLootModifier extends LootModifier {
    public static final Codec<AddWitheredLifeChaliceLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddWitheredLifeChaliceLootModifier::new));

    private static final ResourceLocation WOODLAND_MANSION = ResourceLocation.withDefaultNamespace("chests/woodland_mansion");
    private static final ResourceLocation DESERT_PYRAMID = ResourceLocation.withDefaultNamespace("chests/desert_pyramid");
    private static final ResourceLocation JUNGLE_TEMPLE = ResourceLocation.withDefaultNamespace("chests/jungle_temple");
    private static final ResourceLocation SIMPLE_DUNGEON = ResourceLocation.withDefaultNamespace("chests/simple_dungeon");

    public AddWitheredLifeChaliceLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        double chance = getChance(context.getQueriedLootTableId());
        if (chance > 0.0D && context.getRandom().nextDouble() < chance) {
            generatedLoot.add(new ItemStack(ModItems.WITHERED_LIFE_CHALICE.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_WITHERED_LIFE_CHALICE.get();
    }

    private static double getChance(ResourceLocation lootTableId) {
        if (WOODLAND_MANSION.equals(lootTableId)) {
            return ModServerConfig.WITHERED_LIFE_CHALICE_WOODLAND_MANSION_CHEST_CHANCE.get();
        }
        if (DESERT_PYRAMID.equals(lootTableId)) {
            return ModServerConfig.WITHERED_LIFE_CHALICE_DESERT_PYRAMID_CHEST_CHANCE.get();
        }
        if (JUNGLE_TEMPLE.equals(lootTableId)) {
            return ModServerConfig.WITHERED_LIFE_CHALICE_JUNGLE_TEMPLE_CHEST_CHANCE.get();
        }
        if (SIMPLE_DUNGEON.equals(lootTableId)) {
            return ModServerConfig.WITHERED_LIFE_CHALICE_SIMPLE_DUNGEON_CHEST_CHANCE.get();
        }
        return 0.0D;
    }
}
