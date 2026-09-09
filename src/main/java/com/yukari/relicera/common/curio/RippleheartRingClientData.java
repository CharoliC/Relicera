package com.yukari.relicera.common.curio;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public final class RippleheartRingClientData {
    private static RippleheartRingEffects.ActiveState activeState = RippleheartRingEffects.ActiveState.INACTIVE;
    private static Optional<UUID> pairId = Optional.empty();
    private static Component redWearerName = Component.empty();
    private static Component blueWearerName = Component.empty();

    private RippleheartRingClientData() {
    }

    public static RippleheartRingEffects.ActiveState getActiveState() {
        return activeState;
    }

    public static Optional<UUID> getPairId() {
        return pairId;
    }

    public static Component getRedWearerName() {
        return redWearerName;
    }

    public static Component getBlueWearerName() {
        return blueWearerName;
    }

    public static void update(
            RippleheartRingEffects.ActiveState state,
            Optional<UUID> activePairId,
            Component activeRedWearerName,
            Component activeBlueWearerName
    ) {
        activeState = state;
        pairId = activePairId;
        redWearerName = activeRedWearerName;
        blueWearerName = activeBlueWearerName;
    }

    public static void reset() {
        activeState = RippleheartRingEffects.ActiveState.INACTIVE;
        pairId = Optional.empty();
        redWearerName = Component.empty();
        blueWearerName = Component.empty();
    }
}
