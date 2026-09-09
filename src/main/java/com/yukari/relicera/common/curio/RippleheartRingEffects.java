package com.yukari.relicera.common.curio;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.item.RippleheartRingItem;
import com.yukari.relicera.common.item.RippleheartRingItem.RingSide;
import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.SyncRippleheartRingStatePacket;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class RippleheartRingEffects {
    private static final int STATE_UPDATE_INTERVAL_TICKS = 5;
    private static final int FAILED_SEARCH_RETRY_TICKS = 10;
    private static final int CACHED_PAIR_RESCAN_TICKS = 20;
    private static final UUID RED_ATTACK_SPEED_MODIFIER_ID = UUID.fromString("8cf87d3c-fbe8-4e28-a29a-45baa76b37aa");
    private static final String RED_ATTACK_SPEED_MODIFIER_NAME = "Relicera Rippleheart Ring red attack speed";
    private static final ResourceLocation RIPPLEHEART_RINGS_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "rippleheart_rings");
    private static final String RIPPLEHEART_RINGS_ADVANCEMENT_CRITERION = "wear_active_rippleheart_ring";
    private static final Map<UUID, SyncedPairState> LAST_SYNCED_STATES = new HashMap<>();
    private static final Map<UUID, CachedPartner> CACHED_PARTNERS = new HashMap<>();
    private static final Map<UUID, Long> NEXT_SEARCH_TICKS = new HashMap<>();
    private static final ThreadLocal<Boolean> TRANSFERRING_HARMFUL_EFFECT = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> MIRRORING_HEAL = ThreadLocal.withInitial(() -> false);

    private RippleheartRingEffects() {
    }

    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % STATE_UPDATE_INTERVAL_TICKS == 0) {
            updatePlayerState(player, false);
        }
    }

    public static void forceSync(ServerPlayer player) {
        NEXT_SEARCH_TICKS.remove(player.getUUID());
        updatePlayerState(player, true);
    }

    public static void forgetPlayer(ServerPlayer player) {
        LAST_SYNCED_STATES.remove(player.getUUID());
        NEXT_SEARCH_TICKS.remove(player.getUUID());
        clearCachedPartner(player.getUUID());
    }

    public static Optional<ActivePair> findActivePair(LivingEntity wearer) {
        double range = ModCommonConfig.RIPPLEHEART_RINGS_ACTIVATION_RANGE.get();
        if (range <= 0.0D || !wearer.isAlive() || !(wearer.level() instanceof ServerLevel serverLevel)) {
            clearPairState(wearer.getUUID());
            return Optional.empty();
        }

        Optional<EquippedRing> wornRing = findSingleEquippedRing(wearer);
        if (wornRing.isEmpty()) {
            clearPairState(wearer.getUUID());
            return Optional.empty();
        }

        long gameTime = serverLevel.getGameTime();
        Optional<ActivePair> cachedPair = findCachedPair(serverLevel, wearer, wornRing.get(), range, gameTime);
        if (cachedPair.isPresent()) {
            return cachedPair;
        }

        long nextSearchTick = NEXT_SEARCH_TICKS.getOrDefault(wearer.getUUID(), Long.MIN_VALUE);
        if (gameTime < nextSearchTick) {
            return Optional.empty();
        }

        List<MatchingWearer> matches = findMatchingWearers(wearer, wornRing.get(), range);
        if (matches.size() != 1) {
            rememberFailedSearch(wearer, gameTime);
            return Optional.empty();
        }

        MatchingWearer partner = matches.get(0);
        List<MatchingWearer> reverseMatches = findMatchingWearers(partner.entity(), partner.ring(), range);
        if (reverseMatches.size() != 1 || reverseMatches.get(0).entity() != wearer) {
            rememberFailedSearch(wearer, gameTime);
            return Optional.empty();
        }

        cachePair(wearer, wornRing.get(), partner.entity(), partner.ring(), gameTime);
        return Optional.of(createActivePair(wearer, wornRing.get(), partner.entity(), partner.ring()));
    }

    public static void modifyCombatDamage(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }

        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof LivingEntity attacker && attacker != target) {
            findActivePair(attacker)
                    .filter(pair -> pair.side() == RingSide.RED)
                    .ifPresent(pair -> {
                        double bonus = ModCommonConfig.RIPPLEHEART_RINGS_RED_DAMAGE_BONUS.get();
                        if (bonus > 0.0D) {
                            event.setAmount((float) (event.getAmount() * (1.0D + bonus)));
                        }
                    });
        }

        findActivePair(target)
                .filter(pair -> pair.side() == RingSide.BLUE)
                .ifPresent(pair -> {
                    double reduction = ModCommonConfig.RIPPLEHEART_RINGS_BLUE_DAMAGE_REDUCTION.get();
                    if (reduction > 0.0D) {
                        event.setAmount((float) (event.getAmount() * Math.max(0.0D, 1.0D - reduction)));
                    }
                });
    }

    public static void applyCombatTriggers(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }

        int duration = ModCommonConfig.RIPPLEHEART_RINGS_COMBAT_EFFECT_DURATION_TICKS.get();
        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        Optional<ActivePair> attackerPair = sourceEntity instanceof LivingEntity attacker && attacker != target
                ? findActivePair(attacker).filter(pair -> pair.side() == RingSide.RED)
                : Optional.empty();
        if (attackerPair.map(pair -> pair.partner() == target).orElse(false)) {
            return;
        }

        findActivePair(target)
                .filter(pair -> pair.side() == RingSide.BLUE)
                .ifPresent(pair -> pair.partner().addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        duration,
                        2,
                        false,
                        true,
                        true
                ), target));

        attackerPair
                .ifPresent(pair -> pair.partner().addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        duration,
                        2,
                        false,
                        true,
                        true
                ), pair.wearer()));
    }

    public static void transferHarmfulEffect(MobEffectEvent.Applicable event) {
        LivingEntity wearer = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();
        if (wearer.level().isClientSide()
                || TRANSFERRING_HARMFUL_EFFECT.get()
                || effect.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }

        Optional<ActivePair> activePair = findActivePair(wearer)
                .filter(pair -> pair.side() == RingSide.RED);
        if (activePair.isEmpty()) {
            return;
        }

        event.setResult(Event.Result.DENY);
        TRANSFERRING_HARMFUL_EFFECT.set(true);
        try {
            activePair.get().partner().addEffect(new MobEffectInstance(effect));
        } finally {
            TRANSFERRING_HARMFUL_EFFECT.remove();
        }
    }

    public static void applyHealingEffects(LivingHealEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer.level().isClientSide()
                || event.isCanceled()
                || event.getAmount() <= 0.0F
                || MIRRORING_HEAL.get()) {
            return;
        }

        Optional<ActivePair> activePair = findActivePair(wearer);
        if (activePair.isEmpty()) {
            return;
        }

        ActivePair pair = activePair.get();
        if (pair.side() == RingSide.BLUE) {
            double bonus = ModCommonConfig.RIPPLEHEART_RINGS_BLUE_HEALING_BONUS.get();
            if (bonus > 0.0D) {
                event.setAmount((float) (event.getAmount() * (1.0D + bonus)));
            }
            return;
        }

        float missingHealth = Math.max(0.0F, wearer.getMaxHealth() - wearer.getHealth());
        float mirroredAmount = Math.min(event.getAmount(), missingHealth);
        if (mirroredAmount <= 0.0F) {
            return;
        }

        MIRRORING_HEAL.set(true);
        try {
            pair.partner().heal(mirroredAmount);
        } finally {
            MIRRORING_HEAL.remove();
        }
    }

    private static void updatePlayerState(ServerPlayer player, boolean force) {
        Optional<ActivePair> activePair = findActivePair(player);
        updatePlayerAttackSpeed(player, activePair);

        SyncedPairState state = activePair
                .map(RippleheartRingEffects::createSyncedPairState)
                .orElse(SyncedPairState.INACTIVE);
        SyncedPairState previous = LAST_SYNCED_STATES.put(player.getUUID(), state);
        if (state.activeState() != ActiveState.INACTIVE
                && (previous == null || previous.activeState() == ActiveState.INACTIVE)) {
            awardRippleheartRingsAdvancement(player);
        }
        if (force || !state.equals(previous)) {
            ModNetworking.sendToPlayer(new SyncRippleheartRingStatePacket(
                    state.activeState(),
                    state.pairId(),
                    state.redWearerName(),
                    state.blueWearerName()
            ), player);
        }
    }

    private static void awardRippleheartRingsAdvancement(ServerPlayer player) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(RIPPLEHEART_RINGS_ADVANCEMENT);
        if (advancement != null) {
            player.getAdvancements().award(advancement, RIPPLEHEART_RINGS_ADVANCEMENT_CRITERION);
        }
    }

    private static SyncedPairState createSyncedPairState(ActivePair pair) {
        LivingEntity redWearer = pair.side() == RingSide.RED ? pair.wearer() : pair.partner();
        LivingEntity blueWearer = pair.side() == RingSide.BLUE ? pair.wearer() : pair.partner();
        return new SyncedPairState(
                ActiveState.fromSide(pair.side()),
                RippleheartRingItem.getPairId(pair.ringStack()),
                getSyncedWearerName(redWearer),
                getSyncedWearerName(blueWearer)
        );
    }

    private static Component getSyncedWearerName(LivingEntity entity) {
        return entity.getDisplayName();
    }

    private static void updatePlayerAttackSpeed(ServerPlayer player, Optional<ActivePair> activePair) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }

        double amount = ModCommonConfig.RIPPLEHEART_RINGS_RED_ATTACK_SPEED_BONUS.get();
        boolean active = amount > 0.0D
                && activePair.map(pair -> pair.side() == RingSide.RED).orElse(false);
        AttributeModifier existing = attackSpeed.getModifier(RED_ATTACK_SPEED_MODIFIER_ID);
        if (!active) {
            if (existing != null) {
                attackSpeed.removeModifier(RED_ATTACK_SPEED_MODIFIER_ID);
            }
            return;
        }

        if (existing != null
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL
                && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            attackSpeed.removeModifier(RED_ATTACK_SPEED_MODIFIER_ID);
        }
        attackSpeed.addTransientModifier(new AttributeModifier(
                RED_ATTACK_SPEED_MODIFIER_ID,
                RED_ATTACK_SPEED_MODIFIER_NAME,
                amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    private static Optional<EquippedRing> findSingleEquippedRing(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>();
        CuriosApi.getCuriosInventory(entity).resolve().ifPresent(handler -> {
            handler.findCurios(ModItems.RIPPLEHEART_RING_RED.get()).stream()
                    .map(SlotResult::stack)
                    .forEach(stacks::add);
            handler.findCurios(ModItems.RIPPLEHEART_RING_BLUE.get()).stream()
                    .map(SlotResult::stack)
                    .forEach(stacks::add);
        });

        List<EquippedRing> boundRings = stacks.stream()
                .filter(stack -> stack.getItem() instanceof RippleheartRingItem)
                .map(stack -> {
                    RippleheartRingItem item = (RippleheartRingItem) stack.getItem();
                    return RippleheartRingItem.getPairId(stack)
                            .map(pairId -> new EquippedRing(stack, item.getSide(), pairId))
                            .orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return boundRings.size() == 1 ? Optional.of(boundRings.get(0)) : Optional.empty();
    }

    private static List<MatchingWearer> findMatchingWearers(LivingEntity wearer, EquippedRing ring, double range) {
        double rangeSqr = range * range;
        List<MatchingWearer> matches = new ArrayList<>();
        List<LivingEntity> candidates = wearer.level().getEntitiesOfClass(
                LivingEntity.class,
                wearer.getBoundingBox().inflate(range),
                candidate -> candidate != wearer
                        && candidate.isAlive()
                        && wearer.distanceToSqr(candidate) <= rangeSqr
        );
        for (LivingEntity candidate : candidates) {
            Optional<EquippedRing> candidateRing = findSingleEquippedRing(candidate);
            if (candidateRing.isPresent()
                    && candidateRing.get().side() != ring.side()
                    && candidateRing.get().pairId().equals(ring.pairId())) {
                matches.add(new MatchingWearer(candidate, candidateRing.get()));
                if (matches.size() > 1) {
                    break;
                }
            }
        }
        return matches;
    }

    private static Optional<ActivePair> findCachedPair(
            ServerLevel level,
            LivingEntity wearer,
            EquippedRing wornRing,
            double range,
            long gameTime
    ) {
        CachedPartner cached = CACHED_PARTNERS.get(wearer.getUUID());
        if (cached == null || !cached.pairId().equals(wornRing.pairId()) || cached.side() != wornRing.side()) {
            clearCachedPartner(wearer.getUUID());
            return Optional.empty();
        }

        Entity cachedEntity = level.getEntity(cached.partnerId());
        if (!(cachedEntity instanceof LivingEntity partner)
                || !partner.isAlive()
                || wearer.distanceToSqr(partner) > range * range) {
            clearCachedPartner(wearer.getUUID());
            return Optional.empty();
        }

        Optional<EquippedRing> partnerRing = findSingleEquippedRing(partner);
        if (partnerRing.isEmpty()
                || partnerRing.get().side() == wornRing.side()
                || !partnerRing.get().pairId().equals(wornRing.pairId())) {
            clearCachedPartner(wearer.getUUID());
            return Optional.empty();
        }

        if (gameTime >= cached.nextFullValidationTick()) {
            clearCachedPartner(wearer.getUUID());
            return Optional.empty();
        }

        return Optional.of(createActivePair(wearer, wornRing, partner, partnerRing.get()));
    }

    private static ActivePair createActivePair(
            LivingEntity wearer,
            EquippedRing wornRing,
            LivingEntity partner,
            EquippedRing partnerRing
    ) {
        return new ActivePair(wearer, wornRing.stack(), wornRing.side(), partner, partnerRing.stack());
    }

    private static void cachePair(
            LivingEntity wearer,
            EquippedRing wornRing,
            LivingEntity partner,
            EquippedRing partnerRing,
            long gameTime
    ) {
        long nextFullValidationTick = gameTime + CACHED_PAIR_RESCAN_TICKS;
        CACHED_PARTNERS.put(wearer.getUUID(), new CachedPartner(
                partner.getUUID(),
                wornRing.pairId(),
                wornRing.side(),
                nextFullValidationTick
        ));
        CACHED_PARTNERS.put(partner.getUUID(), new CachedPartner(
                wearer.getUUID(),
                partnerRing.pairId(),
                partnerRing.side(),
                nextFullValidationTick
        ));
        NEXT_SEARCH_TICKS.remove(wearer.getUUID());
        NEXT_SEARCH_TICKS.remove(partner.getUUID());
    }

    private static void rememberFailedSearch(LivingEntity wearer, long gameTime) {
        clearCachedPartner(wearer.getUUID());
        NEXT_SEARCH_TICKS.put(wearer.getUUID(), gameTime + FAILED_SEARCH_RETRY_TICKS);
    }

    private static void clearPairState(UUID wearerId) {
        NEXT_SEARCH_TICKS.remove(wearerId);
        clearCachedPartner(wearerId);
    }

    private static void clearCachedPartner(UUID wearerId) {
        CachedPartner cached = CACHED_PARTNERS.remove(wearerId);
        if (cached == null) {
            return;
        }

        CachedPartner reverse = CACHED_PARTNERS.get(cached.partnerId());
        if (reverse != null && reverse.partnerId().equals(wearerId)) {
            CACHED_PARTNERS.remove(cached.partnerId());
        }
    }

    public enum ActiveState {
        INACTIVE,
        RED,
        BLUE;

        private static ActiveState fromSide(RingSide side) {
            return side == RingSide.RED ? RED : BLUE;
        }
    }

    public record ActivePair(
            LivingEntity wearer,
            ItemStack ringStack,
            RingSide side,
            LivingEntity partner,
            ItemStack partnerRingStack
    ) {
    }

    private record EquippedRing(ItemStack stack, RingSide side, UUID pairId) {
    }

    private record MatchingWearer(LivingEntity entity, EquippedRing ring) {
    }

    private record CachedPartner(UUID partnerId, UUID pairId, RingSide side, long nextFullValidationTick) {
    }

    private record SyncedPairState(
            ActiveState activeState,
            Optional<UUID> pairId,
            Component redWearerName,
            Component blueWearerName
    ) {
        private static final SyncedPairState INACTIVE = new SyncedPairState(
                ActiveState.INACTIVE,
                Optional.empty(),
                Component.empty(),
                Component.empty()
        );
    }
}
