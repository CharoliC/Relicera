package com.yukari.relicera.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class AddHeartOfTheMountainLootModifier extends LootModifier {
    public static final Codec<AddHeartOfTheMountainLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddHeartOfTheMountainLootModifier::new));

    private static final double BASE_DROP_CHANCE = 0.09D;
    private static final double FORTUNE_DROP_CHANCE = 0.03D;

    public AddHeartOfTheMountainLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (blockState == null
                || !blockState.is(Blocks.DEEPSLATE_EMERALD_ORE)
                || tool == null
                || !tool.isCorrectToolForDrops(blockState)
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player)
                || tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0) {
            return generatedLoot;
        }

        int fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        double dropChance = Math.min(1.0D, BASE_DROP_CHANCE + FORTUNE_DROP_CHANCE * fortuneLevel);
        if (context.getRandom().nextDouble() < dropChance) {
            generatedLoot.add(new ItemStack(ModItems.HEART_OF_THE_MOUNTAIN.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_HEART_OF_THE_MOUNTAIN.get();
    }
}
