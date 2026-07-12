package com.yukari.relicera.mixin;

import com.yukari.relicera.common.effect.TempestSprintEffects;
import com.yukari.relicera.common.effect.TempestSprintVisualState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin implements TempestSprintVisualState {
    @Unique
    private static final EntityDataAccessor<Boolean> relicera$TEMPEST_SPRINT_VISUAL = SynchedEntityData.defineId(
            AbstractHorse.class,
            EntityDataSerializers.BOOLEAN
    );

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void relicera$defineTempestSprintVisualData(CallbackInfo ci) {
        ((AbstractHorse) (Object) this).getEntityData().define(relicera$TEMPEST_SPRINT_VISUAL, false);
    }

    @Inject(method = "handleStartJump", at = @At("HEAD"))
    private void relicera$rememberTempestsReinsFullJumpStart(int jumpPower, CallbackInfo ci) {
        if ((Object) this instanceof Horse horse) {
            TempestSprintEffects.rememberFullJump(horse, jumpPower);
        }
    }

    @Inject(method = "onPlayerJump", at = @At("HEAD"))
    private void relicera$rememberTempestsReinsFullJump(int jumpPower, CallbackInfo ci) {
        if ((Object) this instanceof Horse horse) {
            TempestSprintEffects.rememberFullJump(horse, jumpPower);
        }
    }

    @Override
    public void relicera$setTempestSprintVisualActive(boolean active) {
        ((AbstractHorse) (Object) this).getEntityData().set(relicera$TEMPEST_SPRINT_VISUAL, active);
    }

    @Override
    public boolean relicera$isTempestSprintVisualActive() {
        return ((AbstractHorse) (Object) this).getEntityData().get(relicera$TEMPEST_SPRINT_VISUAL);
    }
}
