package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.effect.IluthiasBlessingMobEffect;
import com.yukari.relicera.common.effect.TempestSprintMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ReliceraMod.MOD_ID);

    public static final RegistryObject<MobEffect> ILUTHIAS_BLESSING = MOB_EFFECTS.register("iluthias_blessing", IluthiasBlessingMobEffect::new);
    public static final RegistryObject<MobEffect> TEMPEST_SPRINT = MOB_EFFECTS.register("tempest_sprint", TempestSprintMobEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
