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

public class AddEphemeralBloomLootModifier extends LootModifier {
    public static final Codec<AddEphemeralBloomLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance)
                    .and(Source.CODEC.fieldOf("source").forGetter(modifier -> modifier.source))
                    .apply(instance, AddEphemeralBloomLootModifier::new));

    private static final int NEW_MOON_PHASE = 4;
    private static final ResourceLocation SNIFFER_DIGGING_LOOT_TABLE =
            ResourceLocation.withDefaultNamespace("gameplay/sniffer_digging");
    private static final ResourceLocation STRONGHOLD_LIBRARY_LOOT_TABLE =
            ResourceLocation.withDefaultNamespace("chests/stronghold_library");

    private final Source source;

    public AddEphemeralBloomLootModifier(LootItemCondition[] conditions, Source source) {
        super(conditions);
        this.source = source;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (source.matches(context)
                && context.getRandom().nextDouble() < source.chance()) {
            generatedLoot.add(new ItemStack(ModItems.EPHEMERAL_BLOOM.get()));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_EPHEMERAL_BLOOM.get();
    }

    public enum Source {
        SNIFFER_DIGGING_NEW_MOON("sniffer_digging_new_moon"),
        STRONGHOLD_LIBRARY_CHEST("stronghold_library_chest");

        private static final Codec<Source> CODEC = Codec.STRING.xmap(Source::byName, Source::getSerializedName);
        private final String serializedName;

        Source(String serializedName) {
            this.serializedName = serializedName;
        }

        private String getSerializedName() {
            return serializedName;
        }

        private boolean matches(LootContext context) {
            return switch (this) {
                case SNIFFER_DIGGING_NEW_MOON -> SNIFFER_DIGGING_LOOT_TABLE.equals(context.getQueriedLootTableId())
                        && context.getLevel().dimensionType().moonPhase(context.getLevel().getDayTime()) == NEW_MOON_PHASE;
                case STRONGHOLD_LIBRARY_CHEST -> STRONGHOLD_LIBRARY_LOOT_TABLE.equals(context.getQueriedLootTableId());
            };
        }

        private double chance() {
            return switch (this) {
                case SNIFFER_DIGGING_NEW_MOON -> ModServerConfig.EPHEMERAL_BLOOM_SNIFFER_DIGGING_NEW_MOON_CHANCE.get();
                case STRONGHOLD_LIBRARY_CHEST -> ModServerConfig.EPHEMERAL_BLOOM_STRONGHOLD_LIBRARY_CHEST_CHANCE.get();
            };
        }

        private static Source byName(String name) {
            for (Source source : values()) {
                if (source.serializedName.equals(name)) {
                    return source;
                }
            }
            throw new IllegalArgumentException("Unknown ephemeral bloom loot source: " + name);
        }
    }
}
