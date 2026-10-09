package com.yukari.relicera.common.astral;

public final class AstralObservationClientData {
    private static int observedMoonPhases;
    private static boolean claimedAstralLens;

    private AstralObservationClientData() {
    }

    public static void update(int observedMoonPhaseMask, boolean claimed) {
        observedMoonPhases = AstralObservationData.countObservedMoonPhases(observedMoonPhaseMask);
        claimedAstralLens = claimed;
    }

    public static int getObservedCount() {
        return observedMoonPhases;
    }

    public static boolean hasClaimedAstralLens() {
        return claimedAstralLens;
    }
}
