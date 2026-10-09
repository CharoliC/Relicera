package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.NereiasCrownEffects;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElderGuardian.class)
public abstract class ElderGuardianMixin {
    @Redirect(
            method = "customServerAiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffectUtil;addEffectToPlayersAround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;DLnet/minecraft/world/effect/MobEffectInstance;I)Ljava/util/List;"
            )
    )
    private List<ServerPlayer> relicera$skipNereiasCrownWearers(ServerLevel level, Entity source, Vec3 position,
                                                               double radius, MobEffectInstance effect,
                                                               int displayLimit) {
        return NereiasCrownEffects.addElderGuardianMiningFatigueToNonWearers(level, source, position, radius, effect, displayLimit);
    }
}
