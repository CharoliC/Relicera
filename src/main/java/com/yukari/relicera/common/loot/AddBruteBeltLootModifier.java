package com.yukari.relicera.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

public final class AddBruteBeltLootModifier extends LootModifier {
    public static final Codec<AddBruteBeltLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddBruteBeltLootModifier::new));

    private static final ResourceLocation PIGLIN_BRUTE_LOOT_TABLE =
            ResourceLocation.withDefaultNamespace("entities/piglin_brute");
    private static final double BADGE_DROP_CHANCE_BONUS = 0.15D;

    public AddBruteBeltLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!PIGLIN_BRUTE_LOOT_TABLE.equals(context.getQueriedLootTableId())) {
            return generatedLoot;
        }

        double chance = ModCommonConfig.BRUTE_BELT_PIGLIN_BRUTE_DROP_CHANCE.get();
        if (context.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof LivingEntity killer
                && CuriosApi.getCuriosInventory(killer).resolve()
                .map(handler -> handler.isEquipped(ModItems.BRUTAL_PLUNDER_BADGE.get())).orElse(false)) {
            chance += BADGE_DROP_CHANCE_BONUS;
        }
        if (context.getRandom().nextDouble() < Math.min(1.0D, chance)) {
            generatedLoot.add(new ItemStack(ModItems.BRUTE_BELT.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_BRUTE_BELT.get();
    }
}
