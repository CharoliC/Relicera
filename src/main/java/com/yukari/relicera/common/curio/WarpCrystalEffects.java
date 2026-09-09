package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class WarpCrystalEffects {
    private static final double MAX_SHIFT_DISTANCE = 0.5D;
    private static final int SHIFT_ATTEMPTS = 8;
    private static final int PARTICLE_COUNT = 6;
    private static final Map<LivingEntity, PendingAttack> PENDING_ATTACKS = new WeakHashMap<>();
    private static final Map<LivingEntity, DamageSource> SUPPRESSED_HIT_FEEDBACK = new WeakHashMap<>();
    private static final ThreadLocal<Deque<PreHurtState>> PRE_HURT_STATES = ThreadLocal.withInitial(ArrayDeque::new);

    private WarpCrystalEffects() {
    }

    public static void tryRandomDodge(LivingAttackEvent event) {
        LivingEntity wearer = event.getEntity();
        PENDING_ATTACKS.remove(wearer);
        SUPPRESSED_HIT_FEEDBACK.remove(wearer);
        if (wearer.level().isClientSide() || event.isCanceled() || !isDodgeable(event.getSource()) || !isEquipped(wearer)) {
            return;
        }

        if (wearer.getRandom().nextDouble() < ModCommonConfig.WARP_CRYSTAL_DODGE_CHANCE.get()) {
            event.setCanceled(true);
            playDodgeEffect(wearer);
            return;
        }

        PENDING_ATTACKS.put(wearer, new PendingAttack(event.getSource(), wearer.getAbsorptionAmount()));
    }

    public static void preventLethalDamage(LivingDamageEvent event) {
        LivingEntity wearer = event.getEntity();
        PendingAttack pendingAttack = PENDING_ATTACKS.remove(wearer);
        if (wearer.level().isClientSide()
                || event.isCanceled()
                || pendingAttack == null
                || pendingAttack.source() != event.getSource()
                || !isDodgeable(event.getSource())
                || event.getAmount() < wearer.getHealth()
                || !consumeOne(wearer)) {
            return;
        }

        event.setCanceled(true);
        SUPPRESSED_HIT_FEEDBACK.put(wearer, event.getSource());
        wearer.setAbsorptionAmount(Math.max(wearer.getAbsorptionAmount(), pendingAttack.absorptionAmount()));
        playBreakSound(wearer);
        playDodgeEffect(wearer);
    }

    public static void beginHurt(LivingEntity entity, int invulnerableTime, float lastHurt) {
        PRE_HURT_STATES.get().push(new PreHurtState(entity, invulnerableTime, lastHurt));
    }

    public static PreHurtState consumeSuppressedHitFeedback(LivingEntity entity, DamageSource source) {
        DamageSource suppressedSource = SUPPRESSED_HIT_FEEDBACK.remove(entity);
        if (suppressedSource != source) {
            return null;
        }

        PreHurtState state = PRE_HURT_STATES.get().peek();
        return state != null && state.entity() == entity ? state : null;
    }

    public static void endHurt(LivingEntity entity) {
        Deque<PreHurtState> states = PRE_HURT_STATES.get();
        if (!states.isEmpty() && states.peek().entity() == entity) {
            states.pop();
        }
        if (states.isEmpty()) {
            PRE_HURT_STATES.remove();
        }
    }

    private static boolean isDodgeable(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        return source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.getEntity() != null
                || source.getDirectEntity() != null;
    }

    private static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.WARP_CRYSTAL.get()))
                .orElse(false);
    }

    private static boolean consumeOne(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> {
                    for (SlotResult result : handler.findCurios(ModItems.WARP_CRYSTAL.get())) {
                        handler.setEquippedCurio(
                                result.slotContext().identifier(),
                                result.slotContext().index(),
                                ItemStack.EMPTY
                        );
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private static void playBreakSound(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            level.playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK,
                    entity.getSoundSource(),
                    1.0F,
                    1.0F
            );
            level.playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.GLASS_BREAK,
                    entity.getSoundSource(),
                    1.0F,
                    1.0F
            );
        }
    }

    private static void playDodgeEffect(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        double oldX = entity.getX();
        double oldY = entity.getY();
        double oldZ = entity.getZ();
        level.playSound(null, oldX, oldY, oldZ, SoundEvents.ILLUSIONER_MIRROR_MOVE, entity.getSoundSource(), 1.0F, 1.0F);
        sendParticles(level, entity, oldX, oldY, oldZ);

        if (tryShiftPosition(level, entity, oldX, oldY, oldZ)) {
            sendParticles(level, entity, entity.getX(), entity.getY(), entity.getZ());
        }
    }

    private static boolean tryShiftPosition(ServerLevel level, LivingEntity entity, double originX, double originY, double originZ) {
        for (int attempt = 0; attempt < SHIFT_ATTEMPTS; attempt++) {
            double angle = entity.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(entity.getRandom().nextDouble()) * MAX_SHIFT_DISTANCE;
            double targetX = originX + Math.cos(angle) * distance;
            double targetZ = originZ + Math.sin(angle) * distance;
            AABB targetBounds = entity.getBoundingBox().move(targetX - entity.getX(), 0.0D, targetZ - entity.getZ());

            if (!level.hasChunkAt(BlockPos.containing(targetX, originY, targetZ))
                    || !level.getWorldBorder().isWithinBounds(targetBounds)
                    || !level.noCollision(entity, targetBounds)) {
                continue;
            }

            entity.teleportTo(targetX, originY, targetZ);
            return true;
        }
        return false;
    }

    private static void sendParticles(ServerLevel level, LivingEntity entity, double x, double y, double z) {
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                x,
                y + entity.getBbHeight() * 0.5D,
                z,
                PARTICLE_COUNT,
                entity.getBbWidth() * 0.35D,
                entity.getBbHeight() * 0.25D,
                entity.getBbWidth() * 0.35D,
                0.02D
        );
    }

    private record PendingAttack(DamageSource source, float absorptionAmount) {
    }

    public record PreHurtState(LivingEntity entity, int invulnerableTime, float lastHurt) {
    }
}
