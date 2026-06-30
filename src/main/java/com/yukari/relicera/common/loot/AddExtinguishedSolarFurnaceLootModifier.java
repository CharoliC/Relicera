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

public class AddExtinguishedSolarFurnaceLootModifier extends LootModifier {
    public static final Codec<AddExtinguishedSolarFurnaceLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddExtinguishedSolarFurnaceLootModifier::new));

    private static final ResourceLocation BASTION_TREASURE = ResourceLocation.withDefaultNamespace("chests/bastion_treasure");
    private static final ResourceLocation NETHER_FORTRESS = ResourceLocation.withDefaultNamespace("chests/nether_bridge");
    private static final ResourceLocation VILLAGE_WEAPONSMITH = ResourceLocation.withDefaultNamespace("chests/village/village_weaponsmith");

    public AddExtinguishedSolarFurnaceLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        double chance = getChance(context.getQueriedLootTableId());
        if (chance > 0.0D && context.getRandom().nextDouble() < chance) {
            generatedLoot.add(new ItemStack(ModItems.EXTINGUISHED_SOLAR_FURNACE.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_EXTINGUISHED_SOLAR_FURNACE.get();
    }

    private static double getChance(ResourceLocation lootTableId) {
        if (BASTION_TREASURE.equals(lootTableId)) {
            return ModServerConfig.EXTINGUISHED_SOLAR_FURNACE_BASTION_TREASURE_CHEST_CHANCE.get();
        }
        if (NETHER_FORTRESS.equals(lootTableId)) {
            return ModServerConfig.EXTINGUISHED_SOLAR_FURNACE_NETHER_FORTRESS_CHEST_CHANCE.get();
        }
        if (VILLAGE_WEAPONSMITH.equals(lootTableId)) {
            return ModServerConfig.EXTINGUISHED_SOLAR_FURNACE_WEAPONSMITH_CHEST_CHANCE.get();
        }
        return 0.0D;
    }
}
