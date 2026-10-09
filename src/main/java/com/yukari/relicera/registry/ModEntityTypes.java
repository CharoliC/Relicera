package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.entity.forgeling.Forgeling;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ReliceraMod.MOD_ID);

    public static final RegistryObject<EntityType<Forgeling>> FORGELING = ENTITY_TYPES.register("forgeling", () ->
            EntityType.Builder.of(Forgeling::new, MobCategory.CREATURE)
                    .sized(1.04F, 1.04F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build("forgeling"));

    private ModEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
