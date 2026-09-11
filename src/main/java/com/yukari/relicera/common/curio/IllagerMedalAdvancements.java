package com.yukari.relicera.common.curio;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class IllagerMedalAdvancements {
    private static final ResourceLocation IRON_GOLEM_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "vindicators_medal_extra_1");
    private static final String IRON_GOLEM_CRITERION = "kill_iron_golem_during_raid";

    private IllagerMedalAdvancements() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()
                || !(event.getEntity() instanceof IronGolem golem)
                || !(golem.level() instanceof ServerLevel level)
                || level.getRaidAt(golem.blockPosition()) == null
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || (!VindicatorsMedalEffects.isEquipped(player)
                    && !TurncoatsMedalEffects.isEquipped(player))) {
            return;
        }

        Advancement advancement = player.server.getAdvancements().getAdvancement(IRON_GOLEM_ADVANCEMENT);
        if (advancement != null) {
            player.getAdvancements().award(advancement, IRON_GOLEM_CRITERION);
        }
    }
}
