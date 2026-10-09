package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PastoralMelodyEffects {
    private static final int MAX_TRIGGER_DELAY_TICKS = 100;
    private static final Map<ResourceKey<Level>, List<ScheduledLove>> PENDING_LOVE = new HashMap<>();

    private PastoralMelodyEffects() {
    }

    public static void charmNearbyAnimals(ServerLevel level, Player player) {
        double range = ModCommonConfig.PASTORAL_MELODY_ANIMAL_RANGE.get();
        if (range <= 0.0D) {
            return;
        }

        double rangeSqr = range * range;
        long gameTime = level.getGameTime();
        AABB area = player.getBoundingBox().inflate(range);
        List<ScheduledLove> queue = PENDING_LOVE.computeIfAbsent(level.dimension(), ignored -> new ArrayList<>());

        for (Animal animal : level.getEntitiesOfClass(Animal.class, area, animal -> canEnterLove(animal) && animal.distanceToSqr(player) <= rangeSqr)) {
            int delay = level.random.nextInt(MAX_TRIGGER_DELAY_TICKS + 1);
            queue.add(new ScheduledLove(animal.getUUID(), player.getUUID(), gameTime + delay));
        }
    }

    public static void tickLevel(ServerLevel level) {
        List<ScheduledLove> queue = PENDING_LOVE.get(level.dimension());
        if (queue == null || queue.isEmpty()) {
            return;
        }

        long gameTime = level.getGameTime();
        Iterator<ScheduledLove> iterator = queue.iterator();
        while (iterator.hasNext()) {
            ScheduledLove scheduled = iterator.next();
            if (scheduled.triggerTime() > gameTime) {
                continue;
            }

            iterator.remove();
            triggerLove(level, scheduled);
        }

        if (queue.isEmpty()) {
            PENDING_LOVE.remove(level.dimension());
        }
    }

    public static void shareCooldownWithGoatHorn(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getItem().is(Items.GOAT_HORN)) {
            player.getCooldowns().addCooldown(ModItems.PASTORAL_MELODY.get(), PastoralMelodyItem.USE_DURATION_TICKS);
        }
    }

    private static void triggerLove(ServerLevel level, ScheduledLove scheduled) {
        Entity entity = level.getEntity(scheduled.animalId());
        if (!(entity instanceof Animal animal) || !canEnterLove(animal)) {
            return;
        }

        animal.setInLove(findPlayer(level, scheduled.playerId()));
    }

    private static boolean canEnterLove(Animal animal) {
        return animal.isAlive() && animal.getAge() == 0 && animal.canFallInLove();
    }

    @Nullable
    private static Player findPlayer(ServerLevel level, UUID playerId) {
        Player player = level.getPlayerByUUID(playerId);
        return player != null && player.isAlive() ? player : null;
    }

    private record ScheduledLove(UUID animalId, UUID playerId, long triggerTime) {
    }
}
