package com.yukari.relicera.common.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.astral.AstralLensDamageProtection;
import com.yukari.relicera.common.astral.AstralObservationData;
import com.yukari.relicera.common.astral.AstralObservationTracker;
import com.yukari.relicera.common.block.DreamcatcherBoxSleepRewards;
import com.yukari.relicera.common.curio.AshenTouchEffects;
import com.yukari.relicera.common.curio.BrutalPlunderBadgeEffects;
import com.yukari.relicera.common.curio.FourfoldSherdPendantEffects;
import com.yukari.relicera.common.curio.GranbellsFurnaceEffects;
import com.yukari.relicera.common.curio.IluthiasChaliceEffects;
import com.yukari.relicera.common.curio.NightGlovesEffects;
import com.yukari.relicera.common.curio.NereiasCrownEffects;
import com.yukari.relicera.common.curio.StriderSpursEffects;
import com.yukari.relicera.common.effect.IluthiasBlessingEffects;
import com.yukari.relicera.common.effect.TempestSprintEffects;
import com.yukari.relicera.common.item.RottenTuskEffects;
import com.yukari.relicera.common.item.RippleheartPearlEffects;
import com.yukari.relicera.common.item.SolarEmberEffects;
import com.yukari.relicera.common.item.StormscaleDrops;
import com.yukari.relicera.common.item.TempestsReinsEffects;
import com.yukari.relicera.common.raid.WarfireFragmentAllayEffects;
import com.yukari.relicera.common.raid.WarfireFragmentDrops;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.DropRulesEvent;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID)
public final class CommonGameEvents {
    private CommonGameEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.player instanceof ServerPlayer serverPlayer) {
            AstralObservationTracker.tick(serverPlayer);
            RottenTuskEffects.repelPiglins(serverPlayer);
            SolarEmberEffects.tick(serverPlayer);
        }
        GranbellsFurnaceEffects.tickPlayer(event.player);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            AshenTouchEffects.clearQueuedFires(serverLevel);
            DreamcatcherBoxSleepRewards.tickLevel(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        DreamcatcherBoxSleepRewards.onSleepFinished(event);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        AstralObservationData.copy(event.getOriginal(), event.getEntity());
        GranbellsFurnaceEffects.restoreKeptInventory(event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            AstralObservationData.sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            AstralObservationData.sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            AstralObservationData.sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity && itemEntity.getItem().is(ModItems.ASTRAL_LENS.get())) {
            itemEntity.setGlowingTag(true);
        }
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        if (event.getEntity().getItem().is(ModItems.ASTRAL_LENS.get())) {
            event.setExtraLife(6000);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (TempestsReinsEffects.preventDamage(event)) {
            return;
        }
        if (GranbellsFurnaceEffects.preventFireDamage(event)) {
            return;
        }
        if (IluthiasBlessingEffects.preventDamage(event)) {
            return;
        }
        if (TempestSprintEffects.reduceDamage(event)) {
            return;
        }
        if (NereiasCrownEffects.preventDamage(event)) {
            return;
        }
        if (NereiasCrownEffects.preventAquaticAllyDamage(event)) {
            return;
        }
        NereiasCrownEffects.reduceActiveDamage(event);
        AstralLensDamageProtection.apply(event);
        NightGlovesEffects.applyNightMeleeDamageBonus(event);
        BrutalPlunderBadgeEffects.applyDamageBonus(event);
        AshenTouchEffects.applyMeleeFireEffects(event);
        GranbellsFurnaceEffects.applyOutgoingDamageBonus(event);
        FourfoldSherdPendantEffects.applyDamageEffects(event);
        IluthiasChaliceEffects.applyDamageEffects(event);
        NereiasCrownEffects.rememberWearerTarget(event);
        WarfireFragmentAllayEffects.preventAllayDamage(event);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        IluthiasChaliceEffects.applyRegeneration(event);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        AshenTouchEffects.rememberPreAttackFireState(event);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (TempestsReinsEffects.preventDamage(event)) {
            return;
        }
        GranbellsFurnaceEffects.igniteAttacker(event);
        if (GranbellsFurnaceEffects.preventFireDamage(event)) {
            return;
        }
        if (NereiasCrownEffects.preventDamage(event)) {
            return;
        }
        if (NereiasCrownEffects.preventAquaticAllyDamage(event)) {
            return;
        }
        RottenTuskEffects.preventPiglinDamage(event);
        WarfireFragmentAllayEffects.preventAllayAttack(event);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        GranbellsFurnaceEffects.rememberInventoryForFireOrLavaDeath(event);
        WarfireFragmentDrops.onLivingDeath(event);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (GranbellsFurnaceEffects.cancelKeptInventoryDrops(event)) {
            return;
        }
        RottenTuskEffects.addZoglinDrop(event);
        BrutalPlunderBadgeEffects.addPiglinBarterDrop(event);
        StormscaleDrops.addElderGuardianThunderstormDrop(event);
    }

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        BrutalPlunderBadgeEffects.applyLootingBonus(event);
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        FourfoldSherdPendantEffects.applyExperienceBonus(event);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        FourfoldSherdPendantEffects.applyBreakSpeedBonus(event);
    }

    @SubscribeEvent
    public static void onLivingUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        FourfoldSherdPendantEffects.applyConsumptionBonuses(event);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        NereiasCrownEffects.takeDrownedHeldItems(event);
        if (event.isCanceled()) {
            return;
        }
        RippleheartPearlEffects.feedEntity(event);
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        NereiasCrownEffects.redirectAquaticAllyTarget(event);
    }

    @SubscribeEvent
    public static void onAnvilRepair(AnvilRepairEvent event) {
        GranbellsFurnaceEffects.preventAnvilDamage(event);
    }

    @SubscribeEvent
    public static void onCurioDropRules(DropRulesEvent event) {
        GranbellsFurnaceEffects.keepCuriosForFireOrLavaDeath(event);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        WarfireFragmentAllayEffects.tickAllayAura(event);
        StriderSpursEffects.tickStrider(event);
        GranbellsFurnaceEffects.tickLavaStanding(event.getEntity());
        FourfoldSherdPendantEffects.tickAttributes(event.getEntity());
        IluthiasChaliceEffects.tickImmunities(event.getEntity());
        NereiasCrownEffects.tick(event);
        TempestsReinsEffects.tickHorse(event);
        TempestSprintEffects.tickHorse(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTickLowest(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DreamcatcherBoxSleepRewards.allowEnigmaticCursedSleep(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        NightGlovesEffects.suppressContainerVibrations(event);
    }
}
