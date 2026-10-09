package com.yukari.relicera.common.compat.twilightforest;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.mixin.LivingEntityAccessor;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModParticleTypes;
import com.yukari.relicera.registry.ModSoundEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import twilightforest.capabilities.CapabilityList;
import twilightforest.data.tags.EntityTagGenerator;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFSounds;
import twilightforest.item.LifedrainScepterItem;

public final class ImmortalSoulRingCompat {
    private static final List<PreDefenseHit> PRE_DEFENSE_HITS = new ArrayList<>();
    private static final List<PendingHit> PENDING_HITS = new ArrayList<>();
    private static final List<PendingKill> PENDING_KILLS = new ArrayList<>();
    private static final Map<ServerPlayer, MeleeAttack> MELEE_ATTACKS = new IdentityHashMap<>();
    private static final Map<ServerPlayer, ComboState> COMBOS = new IdentityHashMap<>();
    private static boolean applyingMagicDamage;

    private ImmortalSoulRingCompat() {
    }

    private static boolean isEquipped(LivingEntity entity) {
        return ModItems.IMMORTAL_SOUL_RING.isPresent()
                && CuriosApi.getCuriosInventory(entity).resolve()
                .map(handler -> handler.isEquipped(ModItems.IMMORTAL_SOUL_RING.get()))
                .orElse(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MELEE_ATTACKS.remove(player);
        if (!isEquipped(player)) {
            return;
        }

        Entity target = event.getTarget();
        if (target instanceof PartEntity<?> part) {
            target = part.getParent();
        }
        if (!(target instanceof LivingEntity)) {
            return;
        }

        AttributeInstance weaponSpeed = new AttributeInstance(Attributes.ATTACK_SPEED, attribute -> {});
        player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED)
                .forEach(weaponSpeed::addTransientModifier);
        double speed = weaponSpeed.getValue();
        double charge = player.getAttackStrengthScale(0.5F);
        double points = 100.0D / speed * charge * charge;
        if (speed > 0.0D && Double.isFinite(points) && points > 0.0D) {
            MELEE_ATTACKS.put(player, new MeleeAttack(event, target, player.server.getTickCount(), points));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || applyingMagicDamage
                || event.getSource() instanceof SoulMagicDamageSource) {
            return;
        }
        LivingEntity wearer = findAttacker(event.getSource());
        if (wearer != null && wearer != event.getEntity() && isEquipped(wearer)) {
            PRE_DEFENSE_HITS.add(new PreDefenseHit(event, wearer, level.getServer().getTickCount()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getSource() instanceof SoulMagicDamageSource source) {
            source.damageEvent = event;
            return;
        }
        if (applyingMagicDamage) {
            return;
        }
        LivingEntity wearer = findAttacker(event.getSource());
        if (wearer == null || wearer == event.getEntity() || !isEquipped(wearer)) {
            return;
        }

        MeleeAttack attack = null;
        if (wearer instanceof ServerPlayer player && event.getSource().getDirectEntity() == player
                && event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            MeleeAttack candidate = MELEE_ATTACKS.get(player);
            if (candidate != null && candidate.tick == player.server.getTickCount()
                    && candidate.target == event.getEntity()) {
                attack = candidate;
            }
        }
        PENDING_HITS.add(new PendingHit(event, wearer, attack, takePreDefenseDamage(event, wearer)));
    }

    private static float takePreDefenseDamage(LivingDamageEvent event, LivingEntity wearer) {
        int tick = event.getEntity().level().getServer().getTickCount();
        for (int index = PRE_DEFENSE_HITS.size() - 1; index >= 0; index--) {
            PreDefenseHit hit = PRE_DEFENSE_HITS.get(index);
            LivingHurtEvent hurt = hit.event;
            if (hit.tick == tick && hit.wearer == wearer && hurt.getEntity() == event.getEntity()
                    && hurt.getSource() == event.getSource()) {
                PRE_DEFENSE_HITS.remove(index);
                if (!hurt.isCanceled() && Float.isFinite(hurt.getAmount()) && hurt.getAmount() > 0.0F) {
                    return hurt.getAmount();
                }
            }
        }
        return 0.0F;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity wearer = findAttacker(event.getSource());
        if (wearer != null && wearer != event.getEntity() && isEquipped(wearer)) {
            PENDING_KILLS.add(new PendingKill(event, wearer));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        COMBOS.entrySet().removeIf(entry -> !entry.getKey().isAlive() || entry.getKey().isRemoved()
                || !isEquipped(entry.getKey())
                || shieldCount(entry.getKey()) >= ModCommonConfig.IMMORTAL_SOUL_RING_MAX_SHIELDS.get());

        List<PendingHit> hits = new ArrayList<>(PENDING_HITS);
        PENDING_HITS.clear();
        PRE_DEFENSE_HITS.clear();
        for (PendingHit hit : hits) {
            finishHit(hit);
        }
        MELEE_ATTACKS.clear();
        int tick = event.getServer().getTickCount();
        COMBOS.entrySet().removeIf(entry -> expired(tick, entry.getValue().lastHitTick));

        List<PendingKill> kills = new ArrayList<>(PENDING_KILLS);
        PENDING_KILLS.clear();
        Set<LivingEntity> healedKills = Collections.newSetFromMap(new IdentityHashMap<>());
        for (PendingKill kill : kills) {
            LivingEntity victim = kill.event.getEntity();
            LivingEntity wearer = kill.wearer;
            if (!kill.event.isCanceled() && victim.isDeadOrDying() && !victim.isAlive()
                    && wearer.isAlive() && !wearer.isRemoved() && isEquipped(wearer)
                    && healedKills.add(victim)) {
                float amount = (float) (wearer.getMaxHealth() * ModCommonConfig.IMMORTAL_SOUL_RING_KILL_HEAL_RATIO.get());
                if (Float.isFinite(amount) && amount > 0.0F) {
                    wearer.heal(amount);
                }
                playKillEffects(kill);
            }
        }
    }

    private static void playKillEffects(PendingKill kill) {
        LivingEntity victim = kill.event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || kill.event.getSource().is(TFDamageTypes.LIFEDRAIN)) {
            return;
        }
        if (!victim.getType().is(EntityTagGenerator.LIFEDRAIN_DROPS_NO_FLESH)) {
            LifedrainScepterItem.animateTargetShatter(level, victim);
        }
        if (victim instanceof Mob mob) {
            mob.spawnAnim();
        }
        if (!victim.isSilent()) {
            level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), TFSounds.SCEPTER_DRAIN.get(),
                    victim.getSoundSource(), 1.0F, kill.wearer.getVoicePitch());
        }
    }

    private static void finishHit(PendingHit hit) {
        LivingDamageEvent event = hit.event;
        LivingEntity wearer = hit.wearer;
        LivingEntity target = event.getEntity();
        if (event.isCanceled() || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0F
                || !wearer.isAlive() || wearer.isRemoved() || !isEquipped(wearer)) {
            return;
        }

        if (hit.attack != null && !hit.attack.counted && !hit.attack.event.isCanceled()
                && wearer instanceof ServerPlayer player) {
            hit.attack.counted = true;
            addCombo(player, hit.attack);
        }

        float amount = (float) (hit.preDefenseDamage * ModCommonConfig.IMMORTAL_SOUL_RING_MAGIC_DAMAGE_RATIO.get());
        if (!Float.isFinite(amount) || amount <= 0.0F || !target.isAlive() || target.isRemoved()
                || target.level() != wearer.level()) {
            return;
        }
        LivingEntityAccessor accessor = (LivingEntityAccessor) target;
        float previousLastHurt = accessor.relicera$getLastHurt();
        int previousInvulnerableTime = target.invulnerableTime;
        SoulMagicDamageSource source = new SoulMagicDamageSource(wearer);
        boolean hurt;
        applyingMagicDamage = true;
        try {
            hurt = target.hurt(source, amount);
        } finally {
            applyingMagicDamage = false;
            if (accessor.relicera$getLastHurt() == amount) {
                accessor.relicera$setLastHurt(previousLastHurt);
                if (target.invulnerableTime == 20) {
                    target.invulnerableTime = previousInvulnerableTime;
                }
            }
        }
        LivingDamageEvent magicEvent = source.damageEvent;
        if (hurt && magicEvent != null && !magicEvent.isCanceled()
                && Float.isFinite(magicEvent.getAmount()) && magicEvent.getAmount() > 0.0F
                && target.level() instanceof ServerLevel level) {
            level.sendParticles(ModParticleTypes.LICH_FIRE.get(), target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                    6, target.getBbWidth() * 0.4D, target.getBbHeight() * 0.25D,
                    target.getBbWidth() * 0.4D, 0.008D);
        }
    }

    private static void addCombo(ServerPlayer player, MeleeAttack attack) {
        int limit = ModCommonConfig.IMMORTAL_SOUL_RING_MAX_SHIELDS.get();
        player.getCapability(CapabilityList.SHIELDS).ifPresent(shields -> {
            if (shields.shieldsLeft() >= limit) {
                COMBOS.remove(player);
                return;
            }
            ComboState state = COMBOS.computeIfAbsent(player, ignored -> new ComboState());
            if (expired(attack.tick, state.lastHitTick)) {
                state.points = 0.0D;
            }
            state.lastHitTick = attack.tick;
            state.points += attack.points;
            double threshold = ModCommonConfig.IMMORTAL_SOUL_RING_COMBO_THRESHOLD.get();
            if (state.points >= threshold) {
                int added = (int) Math.min(limit - shields.shieldsLeft(), Math.floor(state.points / threshold));
                state.points -= added * threshold;
                shields.addShields(added, true);
                player.level().playSound(null, player.blockPosition(), ModSoundEvents.IMMORTAL_SOUL_RING_SHIELD.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                if (shields.shieldsLeft() >= limit) {
                    COMBOS.remove(player);
                }
            }
        });
    }

    private static int shieldCount(ServerPlayer player) {
        return player.getCapability(CapabilityList.SHIELDS).map(shields -> shields.shieldsLeft()).orElse(0);
    }

    private static boolean expired(int tick, int lastHitTick) {
        long elapsed = (long) tick - lastHitTick;
        return elapsed < 0 || elapsed > ModCommonConfig.IMMORTAL_SOUL_RING_COMBO_TIMEOUT_TICKS.get();
    }

    private static LivingEntity findAttacker(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity living) {
            return living;
        }
        if (source.getDirectEntity() instanceof Projectile projectile
                && projectile.getOwner() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getFrom().is(ModItems.IMMORTAL_SOUL_RING.get())
                && !event.getTo().is(ModItems.IMMORTAL_SOUL_RING.get())) {
            clearPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) {
            clearPlayer(player);
        }
    }

    private static void clearPlayer(ServerPlayer player) {
        COMBOS.remove(player);
        MeleeAttack attack = MELEE_ATTACKS.remove(player);
        if (attack != null) {
            attack.counted = true;
        }
        PENDING_HITS.removeIf(hit -> hit.wearer == player);
        PRE_DEFENSE_HITS.removeIf(hit -> hit.wearer == player);
        PENDING_KILLS.removeIf(kill -> kill.wearer == player);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PRE_DEFENSE_HITS.clear();
        PENDING_HITS.clear();
        PENDING_KILLS.clear();
        MELEE_ATTACKS.clear();
        COMBOS.clear();
        applyingMagicDamage = false;
    }

    private record PreDefenseHit(LivingHurtEvent event, LivingEntity wearer, int tick) {
    }

    private record PendingHit(LivingDamageEvent event, LivingEntity wearer, MeleeAttack attack, float preDefenseDamage) {
    }

    private record PendingKill(LivingDeathEvent event, LivingEntity wearer) {
    }

    private static final class MeleeAttack {
        private final AttackEntityEvent event;
        private final Entity target;
        private final int tick;
        private final double points;
        private boolean counted;

        private MeleeAttack(AttackEntityEvent event, Entity target, int tick, double points) {
            this.event = event;
            this.target = target;
            this.tick = tick;
            this.points = points;
        }
    }

    private static final class ComboState {
        private double points;
        private int lastHitTick;
    }

    private static final class SoulMagicDamageSource extends DamageSource {
        private LivingDamageEvent damageEvent;

        private SoulMagicDamageSource(LivingEntity wearer) {
            super(wearer.damageSources().indirectMagic(wearer, wearer).typeHolder(), wearer, wearer);
        }

        @Override
        public boolean is(TagKey<DamageType> tag) {
            return DamageTypeTags.BYPASSES_COOLDOWN.equals(tag) || super.is(tag);
        }
    }
}
