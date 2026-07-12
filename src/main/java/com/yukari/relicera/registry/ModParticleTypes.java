package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ReliceraMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> ELECTRIC_SPARK = PARTICLE_TYPES.register("electric_spark", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> GOLDHEART_0 = PARTICLE_TYPES.register("goldheart_0", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> GOLDHEART_1 = PARTICLE_TYPES.register("goldheart_1", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> GOLDHEART_2 = PARTICLE_TYPES.register("goldheart_2", () -> new SimpleParticleType(false));

    private ModParticleTypes() {
    }

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
