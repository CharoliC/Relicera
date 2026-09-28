package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.BaromsCovenantStoneEffects;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class BaromExplosionMixin {
    @Shadow @Final private Map<Player, Vec3> hitPlayers;

    @Redirect(method = "explode", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void relicera$preventBaromExplosionPush(Entity entity, Vec3 velocity) {
        if (!BaromsCovenantStoneEffects.preventsKnockback(entity)) {
            entity.setDeltaMovement(velocity);
        }
    }

    @Inject(method = "explode", at = @At("RETURN"))
    private void relicera$preventBaromClientExplosionPush(CallbackInfo ci) {
        hitPlayers.keySet().removeIf(BaromsCovenantStoneEffects::preventsKnockback);
    }
}
