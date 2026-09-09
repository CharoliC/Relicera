package com.yukari.relicera.mixin;

import com.yukari.relicera.common.item.silkofnight.SilkOfNightDecoration;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.WardenEntitySensor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(WardenEntitySensor.class)
public abstract class WardenEntitySensorMixin {
    @ModifyArg(
            method = "getClosest",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;",
                    ordinal = 0
            ),
            index = 0
    )
    private static Predicate<LivingEntity> relicera$excludeScentlessPlayers(Predicate<LivingEntity> original) {
        return entity -> original.test(entity)
                && (!(entity instanceof Player player) || !SilkOfNightDecoration.isWearing(player));
    }
}
