package com.yukari.relicera.common.effect.furnaceofwar;

import com.yukari.relicera.common.curio.GranbellsFurnaceEffects;
import com.yukari.relicera.registry.ModEffects;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public final class FurnaceOfWarEffects {
    private static final String GRANTED_EFFECT_TAG = "ReliceraFurnaceOfWarGranted";
    private static final double PROJECTILE_BURN_RADIUS = 2.5D;
    private static final int WEAPON_BURN_DELAY_TICKS = 40;
    private static final List<PendingWeaponBurn> PENDING_WEAPON_BURNS = new ArrayList<>();

    private FurnaceOfWarEffects() {
    }

    public static void updateGrantedEffect(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean shouldGrant = entity.isAlive()
                && GranbellsFurnaceEffects.isEquipped(entity)
                && (serverLevel.dimension() == ServerLevel.NETHER || serverLevel.getRaidAt(entity.blockPosition()) != null);
        boolean grantedByRelic = entity.getPersistentData().getBoolean(GRANTED_EFFECT_TAG);

        if (shouldGrant) {
            if (!entity.hasEffect(ModEffects.FURNACE_OF_WAR.get())) {
                entity.addEffect(new MobEffectInstance(
                        ModEffects.FURNACE_OF_WAR.get(),
                        MobEffectInstance.INFINITE_DURATION,
                        0,
                        false,
                        true,
                        true
                ));
                entity.getPersistentData().putBoolean(GRANTED_EFFECT_TAG, true);
            }
        } else if (grantedByRelic) {
            entity.removeEffect(ModEffects.FURNACE_OF_WAR.get());
            entity.getPersistentData().remove(GRANTED_EFFECT_TAG);
        }
    }

    public static void burnNearbyProjectiles(LivingEntity bearer) {
        if (!(bearer.level() instanceof ServerLevel serverLevel) || !bearer.isAlive()) {
            return;
        }

        double radiusSqr = PROJECTILE_BURN_RADIUS * PROJECTILE_BURN_RADIUS;
        List<Projectile> projectiles = serverLevel.getEntitiesOfClass(
                Projectile.class,
                bearer.getBoundingBox().inflate(PROJECTILE_BURN_RADIUS),
                projectile -> isBurnableProjectile(projectile)
                        && projectile.getOwner() != bearer
                        && projectile.distanceToSqr(bearer) <= radiusSqr
        );

        Projectile soundSource = null;
        for (Projectile projectile : projectiles) {
            if (burnProjectile(serverLevel, projectile, false) && soundSource == null) {
                soundSource = projectile;
            }
        }
        if (soundSource != null) {
            playExtinguishSound(serverLevel, soundSource.getX(), soundSource.getY(), soundSource.getZ());
        }
    }

    public static void handleProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof LivingEntity bearer)
                || !bearer.hasEffect(ModEffects.FURNACE_OF_WAR.get())
                || !(bearer.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Projectile projectile = event.getProjectile();
        if (projectile.getOwner() == bearer
                || !isBurnableProjectile(projectile)) {
            return;
        }

        event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        burnProjectile(serverLevel, projectile, true);
    }

    public static void scheduleWeaponBurn(LivingAttackEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)
                || !event.getEntity().hasEffect(ModEffects.FURNACE_OF_WAR.get())) {
            return;
        }

        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker)
                || directEntity != sourceEntity
                || attacker == event.getEntity()
                || attacker instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (!isBurnableWeapon(weapon) || isAlreadyPending(attacker, weapon)) {
            return;
        }

        MinecraftServer server = serverLevel.getServer();
        PENDING_WEAPON_BURNS.add(new PendingWeaponBurn(attacker, weapon, server.getTickCount() + WEAPON_BURN_DELAY_TICKS));
    }

    public static void processPendingWeaponBurns(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        int currentTick = server.getTickCount();
        Iterator<PendingWeaponBurn> iterator = PENDING_WEAPON_BURNS.iterator();
        while (iterator.hasNext()) {
            PendingWeaponBurn pending = iterator.next();
            if (pending.attacker().getServer() != server) {
                iterator.remove();
                continue;
            }
            if (currentTick < pending.dueTick()) {
                continue;
            }

            iterator.remove();
            burnHeldWeapon(pending);
        }
    }

    public static void clearPendingWeaponBurns() {
        PENDING_WEAPON_BURNS.clear();
    }

    private static boolean isBurnableProjectile(Projectile projectile) {
        if (!projectile.isAlive() || projectile.isRemoved()) {
            return false;
        }
        if (projectile.getType().is(EntityTypeTags.ARROWS) || projectile instanceof FireworkRocketEntity) {
            return true;
        }
        return projectile instanceof ThrownPotion potion
                && (potion.getItem().is(Items.SPLASH_POTION) || potion.getItem().is(Items.LINGERING_POTION));
    }

    private static boolean burnProjectile(ServerLevel serverLevel, Projectile projectile, boolean playSound) {
        if (!isBurnableProjectile(projectile)) {
            return false;
        }

        double x = projectile.getX();
        double y = projectile.getY(0.5D);
        double z = projectile.getZ();
        projectile.discard();
        spawnBurnParticles(serverLevel, x, y, z);
        if (playSound) {
            playExtinguishSound(serverLevel, x, y, z);
        }
        return true;
    }

    private static boolean isAlreadyPending(LivingEntity attacker, ItemStack weapon) {
        return PENDING_WEAPON_BURNS.stream()
                .anyMatch(pending -> pending.attacker() == attacker && pending.weapon() == weapon);
    }

    private static boolean isBurnableWeapon(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES));
    }

    private static void burnHeldWeapon(PendingWeaponBurn pending) {
        LivingEntity attacker = pending.attacker();
        if (!attacker.isAlive()
                || !(attacker.level() instanceof ServerLevel serverLevel)
                || attacker.getMainHandItem() != pending.weapon()
                || !isBurnableWeapon(pending.weapon())) {
            return;
        }

        ItemStack smeltingResult = findSmeltingResult(serverLevel, pending.weapon());
        pending.weapon().shrink(1);
        if (pending.weapon().isEmpty()) {
            attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (!smeltingResult.isEmpty()) {
            attacker.spawnAtLocation(smeltingResult);
        }

        double x = attacker.getX();
        double y = attacker.getY(0.65D);
        double z = attacker.getZ();
        spawnBurnParticles(serverLevel, x, y, z);
        playExtinguishSound(serverLevel, x, y, z);
    }

    private static ItemStack findSmeltingResult(ServerLevel serverLevel, ItemStack weapon) {
        SimpleContainer input = new SimpleContainer(weapon.copyWithCount(1));
        return serverLevel.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, input, serverLevel)
                .map(recipe -> recipe.assemble(input, serverLevel.registryAccess()))
                .filter(result -> !result.isEmpty())
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    private static void spawnBurnParticles(ServerLevel serverLevel, double x, double y, double z) {
        serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 6, 0.18D, 0.18D, 0.18D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 4, 0.16D, 0.16D, 0.16D, 0.015D);
    }

    private static void playExtinguishSound(ServerLevel serverLevel, double x, double y, double z) {
        serverLevel.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.7F, 0.9F);
    }

    private record PendingWeaponBurn(LivingEntity attacker, ItemStack weapon, int dueTick) {
    }
}
