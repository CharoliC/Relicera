package com.yukari.relicera.registry;

import com.mojang.serialization.Codec;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.loot.AddEphemeralBloomLootModifier;
import com.yukari.relicera.common.loot.AddExtinguishedSolarFurnaceLootModifier;
import com.yukari.relicera.common.loot.AddFeysilverIngotLootModifier;
import com.yukari.relicera.common.loot.AddForgottenThreadLootModifier;
import com.yukari.relicera.common.loot.AddWitheredLifeChaliceLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ReliceraMod.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_FORGOTTEN_THREAD =
            LOOT_MODIFIER_SERIALIZERS.register("add_forgotten_thread", () -> AddForgottenThreadLootModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_EPHEMERAL_BLOOM =
            LOOT_MODIFIER_SERIALIZERS.register("add_ephemeral_bloom", () -> AddEphemeralBloomLootModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_FEYSILVER_INGOT =
            LOOT_MODIFIER_SERIALIZERS.register("add_feysilver_ingot", () -> AddFeysilverIngotLootModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_EXTINGUISHED_SOLAR_FURNACE =
            LOOT_MODIFIER_SERIALIZERS.register("add_extinguished_solar_furnace", () -> AddExtinguishedSolarFurnaceLootModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_WITHERED_LIFE_CHALICE =
            LOOT_MODIFIER_SERIALIZERS.register("add_withered_life_chalice", () -> AddWitheredLifeChaliceLootModifier.CODEC);

    private ModLootModifiers() {
    }

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}
