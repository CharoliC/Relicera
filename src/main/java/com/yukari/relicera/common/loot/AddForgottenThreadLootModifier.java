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

import java.util.Set;

public class AddForgottenThreadLootModifier extends LootModifier {
    public static final Codec<AddForgottenThreadLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance)
                    .and(Source.CODEC.fieldOf("source").forGetter(modifier -> modifier.source))
                    .apply(instance, AddForgottenThreadLootModifier::new));

    private static final Set<ResourceLocation> VILLAGE_CHEST_LOOT_TABLES = Set.of(
            ResourceLocation.withDefaultNamespace("chests/village/village_armorer"),
            ResourceLocation.withDefaultNamespace("chests/village/village_butcher"),
            ResourceLocation.withDefaultNamespace("chests/village/village_cartographer"),
            ResourceLocation.withDefaultNamespace("chests/village/village_desert_house"),
            ResourceLocation.withDefaultNamespace("chests/village/village_fisher"),
            ResourceLocation.withDefaultNamespace("chests/village/village_fletcher"),
            ResourceLocation.withDefaultNamespace("chests/village/village_mason"),
            ResourceLocation.withDefaultNamespace("chests/village/village_plains_house"),
            ResourceLocation.withDefaultNamespace("chests/village/village_savanna_house"),
            ResourceLocation.withDefaultNamespace("chests/village/village_shepherd"),
            ResourceLocation.withDefaultNamespace("chests/village/village_snowy_house"),
            ResourceLocation.withDefaultNamespace("chests/village/village_taiga_house"),
            ResourceLocation.withDefaultNamespace("chests/village/village_tannery"),
            ResourceLocation.withDefaultNamespace("chests/village/village_temple"),
            ResourceLocation.withDefaultNamespace("chests/village/village_toolsmith"),
            ResourceLocation.withDefaultNamespace("chests/village/village_weaponsmith")
    );
    private static final Set<ResourceLocation> ANCIENT_CITY_CHEST_LOOT_TABLES = Set.of(
            ResourceLocation.withDefaultNamespace("chests/ancient_city"),
            ResourceLocation.withDefaultNamespace("chests/ancient_city_ice_box")
    );
    private static final ResourceLocation CAT_MORNING_GIFT_LOOT_TABLE =
            ResourceLocation.withDefaultNamespace("gameplay/cat_morning_gift");

    private final Source source;

    public AddForgottenThreadLootModifier(LootItemCondition[] conditions, Source source) {
        super(conditions);
        this.source = source;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (source.matches(context.getQueriedLootTableId())
                && context.getRandom().nextDouble() < source.chance()) {
            generatedLoot.add(new ItemStack(ModItems.FORGOTTEN_THREAD.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_FORGOTTEN_THREAD.get();
    }

    public enum Source {
        VILLAGE_CHEST("village_chest"),
        ANCIENT_CITY_CHEST("ancient_city_chest"),
        CAT_GIFT("cat_gift");

        private static final Codec<Source> CODEC = Codec.STRING.xmap(Source::byName, Source::getSerializedName);
        private final String serializedName;

        Source(String serializedName) {
            this.serializedName = serializedName;
        }

        private String getSerializedName() {
            return serializedName;
        }

        private boolean matches(ResourceLocation lootTableId) {
            return switch (this) {
                case VILLAGE_CHEST -> VILLAGE_CHEST_LOOT_TABLES.contains(lootTableId);
                case ANCIENT_CITY_CHEST -> ANCIENT_CITY_CHEST_LOOT_TABLES.contains(lootTableId);
                case CAT_GIFT -> CAT_MORNING_GIFT_LOOT_TABLE.equals(lootTableId);
            };
        }

        private double chance() {
            return switch (this) {
                case VILLAGE_CHEST -> ModServerConfig.FORGOTTEN_THREAD_VILLAGE_CHEST_CHANCE.get();
                case ANCIENT_CITY_CHEST -> ModServerConfig.FORGOTTEN_THREAD_ANCIENT_CITY_CHEST_CHANCE.get();
                case CAT_GIFT -> ModServerConfig.FORGOTTEN_THREAD_CAT_GIFT_CHANCE.get();
            };
        }

        private static Source byName(String name) {
            for (Source source : values()) {
                if (source.serializedName.equals(name)) {
                    return source;
                }
            }
            throw new IllegalArgumentException("Unknown forgotten thread loot source: " + name);
        }
    }
}
