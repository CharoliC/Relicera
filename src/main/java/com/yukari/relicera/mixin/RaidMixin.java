package com.yukari.relicera.mixin;

import com.yukari.relicera.common.raid.RaidVictoryRewards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/critereon/PlayerTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;)V"
            ),
            index = 0
    )
    private ServerPlayer relicera$grantRaidVictoryRewards(ServerPlayer player) {
        RaidVictoryRewards.grant(player);
        return player;
    }
}
