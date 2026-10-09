package com.yukari.relicera.common.item.feysilver;

public final class FeysilverForgingClientData {
    private static boolean learnedVolumeOne;

    private FeysilverForgingClientData() {
    }

    public static void update(boolean learned) {
        learnedVolumeOne = learned;
    }

    public static boolean hasVolumeOne() {
        return learnedVolumeOne;
    }
}
