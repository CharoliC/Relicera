package com.yukari.relicera.common.astral;

import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.SyncAstralObservationPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class AstralObservationData {
    public static final int REQUIRED_MOON_PHASE_COUNT = 8;
    private static final int ALL_MOON_PHASES_MASK = (1 << REQUIRED_MOON_PHASE_COUNT) - 1;
    private static final String ROOT_KEY = "ReliceraAstralObservation";
    private static final String OBSERVED_MOON_PHASES_KEY = "ObservedMoonPhases";
    private static final String CLAIMED_ASTRAL_LENS_KEY = "ClaimedAstralLens";

    private AstralObservationData() {
    }

    public static int getObservedMoonPhaseMask(Player player) {
        return getData(player).getInt(OBSERVED_MOON_PHASES_KEY);
    }

    public static boolean hasClaimedAstralLens(Player player) {
        return getData(player).getBoolean(CLAIMED_ASTRAL_LENS_KEY);
    }

    public static boolean observeMoonPhase(ServerPlayer player, int moonPhase) {
        int clampedMoonPhase = moonPhase & 7;
        int observedMoonPhases = getObservedMoonPhaseMask(player);
        int moonPhaseBit = 1 << clampedMoonPhase;
        if ((observedMoonPhases & moonPhaseBit) != 0) {
            return false;
        }

        CompoundTag data = getData(player);
        data.putInt(OBSERVED_MOON_PHASES_KEY, observedMoonPhases | moonPhaseBit);
        setData(player, data);
        sync(player);
        return true;
    }

    public static boolean hasObservedAllMoonPhases(Player player) {
        return (getObservedMoonPhaseMask(player) & ALL_MOON_PHASES_MASK) == ALL_MOON_PHASES_MASK;
    }

    public static void setClaimedAstralLens(ServerPlayer player) {
        CompoundTag data = getData(player);
        data.putBoolean(CLAIMED_ASTRAL_LENS_KEY, true);
        setData(player, data);
        sync(player);
    }

    public static void copy(Player original, Player target) {
        target.getPersistentData().put(ROOT_KEY, getData(original).copy());
    }

    public static void sync(ServerPlayer player) {
        ModNetworking.sendToPlayer(
                new SyncAstralObservationPacket(getObservedMoonPhaseMask(player), hasClaimedAstralLens(player)),
                player
        );
    }

    public static int countObservedMoonPhases(int observedMoonPhaseMask) {
        return Integer.bitCount(observedMoonPhaseMask & ALL_MOON_PHASES_MASK);
    }

    private static CompoundTag getData(Player player) {
        return player.getPersistentData().getCompound(ROOT_KEY);
    }

    private static void setData(Player player, CompoundTag data) {
        player.getPersistentData().put(ROOT_KEY, data);
    }
}
