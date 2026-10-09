package com.yukari.relicera.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class AddReinforcedDeepslateLootModifier extends LootModifier {
    public static final Codec<AddReinforcedDeepslateLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddReinforcedDeepslateLootModifier::new));

    private static final ResourceLocation LOOT_TABLE =
            ResourceLocation.withDefaultNamespace("blocks/reinforced_deepslate");

    public AddReinforcedDeepslateLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ModItems.DEPTHBREAKER.isPresent() || !LOOT_TABLE.equals(context.getQueriedLootTableId())) {
            return generatedLoot;
        }
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (state != null && state.is(Blocks.REINFORCED_DEEPSLATE)
                && tool != null && tool.is(ModItems.DEPTHBREAKER.get())
                && generatedLoot.stream().noneMatch(stack -> stack.is(Blocks.REINFORCED_DEEPSLATE.asItem()))) {
            generatedLoot.add(new ItemStack(Blocks.REINFORCED_DEEPSLATE));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_REINFORCED_DEEPSLATE.get();
    }
}
