package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> BROKEN_RELICS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "broken_relics"));
    public static final TagKey<Item> REPAIRED_RELICS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "relicera_relic"));
    public static final TagKey<Item> DECORATED_POT_SHERDS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "decorated_pot_sherds"));
    public static final TagKey<EntityType<?>> HUMANOIDS = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "humanoids")
    );

    private ModTags() {
    }
}
