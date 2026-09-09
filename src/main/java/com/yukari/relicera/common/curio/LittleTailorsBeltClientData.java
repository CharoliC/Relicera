package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;

public final class LittleTailorsBeltClientData {
    private static final long SHAKE_CACHE_TICKS = 5L;
    private static final Set<UUID> SEEN_THROUGH = new HashSet<>();
    private static final WeakHashMap<LivingEntity, CachedShakeState> SHAKE_CACHE = new WeakHashMap<>();

    private LittleTailorsBeltClientData() {
    }

    public static void markSeenThrough(UUID entityId) {
        SEEN_THROUGH.add(entityId);
        SHAKE_CACHE.clear();
    }

    public static boolean shouldShake(LivingEntity entity) {
        if (!LittleTailorsBeltEffects.isAffectedHumanoid(entity) || SEEN_THROUGH.contains(entity.getUUID())) {
            return false;
        }

        long gameTime = entity.level().getGameTime();
        CachedShakeState cached = SHAKE_CACHE.get(entity);
        if (cached != null && gameTime < cached.expiresAt()) {
            return cached.shaking();
        }

        double range = ModCommonConfig.LITTLE_TAILORS_BELT_REACTION_RANGE.get();
        boolean shaking = range > 0.0D && !entity.level().getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(range),
                wearer -> wearer != entity && wearer.isAlive() && LittleTailorsBeltEffects.isEquipped(wearer)
        ).isEmpty();
        SHAKE_CACHE.put(entity, new CachedShakeState(shaking, gameTime + SHAKE_CACHE_TICKS));
        return shaking;
    }

    public static void clear() {
        SEEN_THROUGH.clear();
        SHAKE_CACHE.clear();
    }

    private record CachedShakeState(boolean shaking, long expiresAt) {
    }
}
