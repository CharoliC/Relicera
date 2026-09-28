package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.BaromsCovenantStoneEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Raids.class)
public abstract class BaromRaidsMixin {
    @Inject(method = "createOrExtendRaid", at = @At("HEAD"), cancellable = true)
    private void relicera$preserveOminousOmen(ServerPlayer player, CallbackInfoReturnable<Raid> cir) {
        if (player.hasEffect(MobEffects.BAD_OMEN)
                && BaromsCovenantStoneEffects.hasContract(player, EntityType.PILLAGER)) {
            cir.setReturnValue(null);
        }
    }
}
