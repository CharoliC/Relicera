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

public class AddDriedCrownLootModifier extends LootModifier {
    public static final Codec<AddDriedCrownLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddDriedCrownLootModifier::new));

    private static final ResourceLocation SHIPWRECK_TREASURE =
            ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure");
    private static final ResourceLocation UNDERWATER_RUIN_BIG =
            ResourceLocation.withDefaultNamespace("chests/underwater_ruin_big");
    private static final ResourceLocation UNDERWATER_RUIN_SMALL =
            ResourceLocation.withDefaultNamespace("chests/underwater_ruin_small");
    private static final ResourceLocation BURIED_TREASURE =
            ResourceLocation.withDefaultNamespace("chests/buried_treasure");

    public AddDriedCrownLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        double chance = getChance(context.getQueriedLootTableId());
        if (chance > 0.0D && context.getRandom().nextDouble() < chance) {
            generatedLoot.add(new ItemStack(ModItems.DRIED_CROWN.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_DRIED_CROWN.get();
    }

    private static double getChance(ResourceLocation lootTableId) {
        if (SHIPWRECK_TREASURE.equals(lootTableId)) {
            return ModCommonConfig.DRIED_CROWN_SHIPWRECK_TREASURE_CHEST_CHANCE.get();
        }
        if (UNDERWATER_RUIN_BIG.equals(lootTableId) || UNDERWATER_RUIN_SMALL.equals(lootTableId)) {
            return ModCommonConfig.DRIED_CROWN_UNDERWATER_RUIN_CHEST_CHANCE.get();
        }
        if (BURIED_TREASURE.equals(lootTableId)) {
            return ModCommonConfig.DRIED_CROWN_BURIED_TREASURE_CHEST_CHANCE.get();
        }
        return 0.0D;
    }
}
