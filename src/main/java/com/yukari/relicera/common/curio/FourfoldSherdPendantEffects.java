package com.yukari.relicera.common.curio;

import com.mojang.datafixers.util.Pair;
import com.yukari.relicera.common.item.FourfoldSherdPendantItem;
import com.yukari.relicera.registry.ModItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class FourfoldSherdPendantEffects {
    private static final Map<Item, SherdPattern> PATTERNS_BY_ITEM = new HashMap<>();

    private static final UUID EXPLORER_SPEED_ID = UUID.fromString("f42ac61c-55d4-4b2d-987b-167bc6a957b8");
    private static final UUID PLENTY_LUCK_ID = UUID.fromString("dbff8a4b-7421-47b8-9c29-301a66f75b83");
    private static final UUID SHELTER_ARMOR_ID = UUID.fromString("96bccd70-7f95-4cc0-b7f2-bfa0242323d4");
    private static final UUID BLADE_DAMAGE_ID = UUID.fromString("4cccb1d7-995c-46d5-8b95-cf28f98f7890");
    private static final UUID HEART_HEALTH_ID = UUID.fromString("280b26ab-c7c2-4303-9341-1380afc9f646");
    private static final UUID ARMS_UP_BLOCK_REACH_ID = UUID.fromString("342311dd-aad7-4a8f-8ce4-db22a12a5029");
    private static final UUID FRIEND_KNOCKBACK_RESISTANCE_ID = UUID.fromString("8a90d0a9-f868-4490-93fb-d14686fa1bf5");

    private static final String EXPLORER_SPEED_NAME = "Relicera fourfold sherd pendant explorer speed";
    private static final String PLENTY_LUCK_NAME = "Relicera fourfold sherd pendant plenty luck";
    private static final String SHELTER_ARMOR_NAME = "Relicera fourfold sherd pendant shelter armor";
    private static final String BLADE_DAMAGE_NAME = "Relicera fourfold sherd pendant blade damage";
    private static final String HEART_HEALTH_NAME = "Relicera fourfold sherd pendant heart health";
    private static final String ARMS_UP_BLOCK_REACH_NAME = "Relicera fourfold sherd pendant arms up block reach";
    private static final String FRIEND_KNOCKBACK_RESISTANCE_NAME = "Relicera fourfold sherd pendant friend knockback resistance";

    static {
        register(SherdPattern.SNORT, Items.SNORT_POTTERY_SHERD);
        register(SherdPattern.BREWER, Items.BREWER_POTTERY_SHERD);
        register(SherdPattern.MOURNER, Items.MOURNER_POTTERY_SHERD);
        register(SherdPattern.EXPLORER, Items.EXPLORER_POTTERY_SHERD);
        register(SherdPattern.HEARTBREAK, Items.HEARTBREAK_POTTERY_SHERD);
        register(SherdPattern.ARMS_UP, Items.ARMS_UP_POTTERY_SHERD);
        register(SherdPattern.ARCHER, Items.ARCHER_POTTERY_SHERD);
        register(SherdPattern.DANGER, Items.DANGER_POTTERY_SHERD);
        register(SherdPattern.PLENTY, Items.PLENTY_POTTERY_SHERD);
        register(SherdPattern.SHEAF, Items.SHEAF_POTTERY_SHERD);
        register(SherdPattern.SHELTER, Items.SHELTER_POTTERY_SHERD);
        register(SherdPattern.BLADE, Items.BLADE_POTTERY_SHERD);
        register(SherdPattern.MINER, Items.MINER_POTTERY_SHERD);
        register(SherdPattern.SKULL, Items.SKULL_POTTERY_SHERD);
        register(SherdPattern.HEART, Items.HEART_POTTERY_SHERD);
        register(SherdPattern.PRIZE, Items.PRIZE_POTTERY_SHERD);
        register(SherdPattern.FRIEND, Items.FRIEND_POTTERY_SHERD);
        register(SherdPattern.ANGLER, Items.ANGLER_POTTERY_SHERD);
        register(SherdPattern.HOWL, Items.HOWL_POTTERY_SHERD);
        register(SherdPattern.BURN, Items.BURN_POTTERY_SHERD);
    }

    private FourfoldSherdPendantEffects() {
    }

    public static int countInStack(ItemStack stack, SherdPattern pattern) {
        if (stack.isEmpty() || !stack.is(ModItems.FOURFOLD_SHERD_PENDANT.get()) || !stack.hasTag()) {
            return 0;
        }
        if (!stack.getTag().contains(FourfoldSherdPendantItem.SHERDS_TAG, Tag.TAG_COMPOUND)) {
            return 0;
        }

        int count = 0;
        ItemStackHandler sherds = new ItemStackHandler(FourfoldSherdPendantItem.SHERD_SLOT_COUNT);
        sherds.deserializeNBT(stack.getTag().getCompound(FourfoldSherdPendantItem.SHERDS_TAG));
        for (int slot = 0; slot < sherds.getSlots(); slot++) {
            ItemStack sherd = sherds.getStackInSlot(slot);
            if (!sherd.isEmpty() && PATTERNS_BY_ITEM.get(sherd.getItem()) == pattern) {
                count++;
            }
        }
        return count;
    }

    public static List<ItemStack> getSherdsInStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModItems.FOURFOLD_SHERD_PENDANT.get()) || !stack.hasTag()) {
            return List.of();
        }
        if (!stack.getTag().contains(FourfoldSherdPendantItem.SHERDS_TAG, Tag.TAG_COMPOUND)) {
            return List.of();
        }

        ItemStackHandler sherds = new ItemStackHandler(FourfoldSherdPendantItem.SHERD_SLOT_COUNT);
        sherds.deserializeNBT(stack.getTag().getCompound(FourfoldSherdPendantItem.SHERDS_TAG));
        return java.util.stream.IntStream.range(0, sherds.getSlots())
                .mapToObj(slot -> sherds.getStackInSlot(slot).copy())
                .filter(sherd -> !sherd.isEmpty())
                .toList();
    }

    public static SherdPattern getPattern(ItemStack stack) {
        return stack.isEmpty() ? null : PATTERNS_BY_ITEM.get(stack.getItem());
    }

    public static int countEquipped(LivingEntity entity, SherdPattern pattern) {
        return getEquippedPendants(entity).stream()
                .mapToInt(stack -> countInStack(stack, pattern))
                .sum();
    }

    public static void tickAttributes(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        applyAttribute(entity, Attributes.MOVEMENT_SPEED, EXPLORER_SPEED_ID, EXPLORER_SPEED_NAME,
                0.20D * countEquipped(entity, SherdPattern.EXPLORER), AttributeModifier.Operation.MULTIPLY_TOTAL);
        applyAttribute(entity, Attributes.LUCK, PLENTY_LUCK_ID, PLENTY_LUCK_NAME,
                countEquipped(entity, SherdPattern.PLENTY), AttributeModifier.Operation.ADDITION);
        applyAttribute(entity, Attributes.ARMOR, SHELTER_ARMOR_ID, SHELTER_ARMOR_NAME,
                4.0D * countEquipped(entity, SherdPattern.SHELTER), AttributeModifier.Operation.ADDITION);
        applyAttribute(entity, Attributes.ATTACK_DAMAGE, BLADE_DAMAGE_ID, BLADE_DAMAGE_NAME,
                0.10D * countEquipped(entity, SherdPattern.BLADE), AttributeModifier.Operation.MULTIPLY_TOTAL);
        applyAttribute(entity, Attributes.MAX_HEALTH, HEART_HEALTH_ID, HEART_HEALTH_NAME,
                4.0D * countEquipped(entity, SherdPattern.HEART), AttributeModifier.Operation.ADDITION);
        applyAttribute(entity, ForgeMod.BLOCK_REACH.get(), ARMS_UP_BLOCK_REACH_ID, ARMS_UP_BLOCK_REACH_NAME,
                countEquipped(entity, SherdPattern.ARMS_UP), AttributeModifier.Operation.ADDITION);
        applyAttribute(entity, Attributes.KNOCKBACK_RESISTANCE, FRIEND_KNOCKBACK_RESISTANCE_ID, FRIEND_KNOCKBACK_RESISTANCE_NAME,
                countEquipped(entity, SherdPattern.FRIEND), AttributeModifier.Operation.ADDITION);

        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    public static void applyDamageEffects(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();

        int danger = countEquipped(target, SherdPattern.DANGER);
        if (danger > 0 && source.is(DamageTypeTags.IS_EXPLOSION)) {
            event.setAmount(event.getAmount() * Math.max(0.0F, 1.0F - 0.50F * danger));
        }

        LivingEntity attacker = getResponsibleAttacker(source.getEntity(), source.getDirectEntity());
        if (attacker == null) {
            return;
        }

        int heartbreak = countEquipped(attacker, SherdPattern.HEARTBREAK);
        if (heartbreak > 0 && attacker.getHealth() / attacker.getMaxHealth() < 0.30F) {
            event.setAmount(event.getAmount() * (1.0F + 0.25F * heartbreak));
        }

        int skull = countEquipped(attacker, SherdPattern.SKULL);
        if (skull > 0 && target.getMobType() == MobType.UNDEAD) {
            event.setAmount(event.getAmount() * (1.0F + 0.15F * skull));
        }

        int archer = countEquipped(attacker, SherdPattern.ARCHER);
        if (archer > 0 && source.getDirectEntity() instanceof AbstractArrow) {
            event.setAmount(event.getAmount() * (1.0F + 0.20F * archer));
        }

        int burn = countEquipped(attacker, SherdPattern.BURN);
        if (burn > 0 && target.isOnFire()) {
            attacker.heal(event.getAmount() * 0.20F * burn);
        }

        if (source.getEntity() instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player owner) {
            int howl = countEquipped(owner, SherdPattern.HOWL);
            if (howl > 0) {
                event.setAmount(event.getAmount() * (1.0F + 0.20F * howl));
            }
        }
    }

    public static void applyExperienceBonus(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null) {
            return;
        }

        int mourner = countEquipped(player, SherdPattern.MOURNER);
        if (mourner > 0) {
            event.setDroppedExperience(Math.round(event.getDroppedExperience() * (1.0F + 0.50F * mourner)));
        }
    }

    public static void applyBreakSpeedBonus(PlayerEvent.BreakSpeed event) {
        int miner = countEquipped(event.getEntity(), SherdPattern.MINER);
        if (miner > 0) {
            event.setNewSpeed(event.getNewSpeed() * (1.0F + 0.10F * miner));
        }
    }

    public static void applyConsumptionBonuses(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        ItemStack consumed = event.getItem();
        int sheaf = countEquipped(entity, SherdPattern.SHEAF);
        if (sheaf > 0 && entity instanceof Player player && consumed.isEdible()) {
            player.getFoodData().eat(sheaf, 0.0F);
        }

        int brewer = countEquipped(entity, SherdPattern.BREWER);
        if (brewer > 0) {
            extendBeneficialEffects(entity, consumed, brewer);
        }
    }

    private static void extendBeneficialEffects(LivingEntity entity, ItemStack consumed, int brewer) {
        for (MobEffectInstance effect : getConsumedEffects(consumed, entity)) {
            if (effect == null || !effect.getEffect().isBeneficial() || effect.getDuration() <= 0) {
                continue;
            }

            MobEffectInstance active = entity.getEffect(effect.getEffect());
            if (active == null) {
                continue;
            }

            int addedDuration = Math.round(effect.getDuration() * 0.20F * brewer);
            if (addedDuration <= 0) {
                continue;
            }

            entity.addEffect(new MobEffectInstance(
                    active.getEffect(),
                    active.getDuration() + addedDuration,
                    active.getAmplifier(),
                    active.isAmbient(),
                    active.isVisible(),
                    active.showIcon()
            ));
        }
    }

    private static List<MobEffectInstance> getConsumedEffects(ItemStack consumed, LivingEntity entity) {
        List<MobEffectInstance> potionEffects = PotionUtils.getMobEffects(consumed);
        if (!potionEffects.isEmpty()) {
            return potionEffects;
        }

        FoodProperties food = consumed.getFoodProperties(entity);
        if (food == null) {
            return List.of();
        }

        return food.getEffects().stream()
                .map(Pair::getFirst)
                .toList();
    }

    private static List<ItemStack> getEquippedPendants(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.findCurios(ModItems.FOURFOLD_SHERD_PENDANT.get()).stream()
                        .map(SlotResult::stack)
                        .toList())
                .orElse(List.of());
    }

    private static LivingEntity getResponsibleAttacker(Entity sourceEntity, Entity directEntity) {
        if (sourceEntity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    private static void applyAttribute(LivingEntity entity, Attribute attribute, UUID id, String name, double amount,
                                       AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(id);
        if (amount <= 0.0D) {
            if (existing != null) {
                instance.removeModifier(id);
            }
            return;
        }

        if (existing != null && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }

    private static void register(SherdPattern pattern, Item item) {
        PATTERNS_BY_ITEM.put(item, pattern);
    }

    public enum SherdPattern {
        SNORT,
        BREWER,
        MOURNER,
        EXPLORER,
        HEARTBREAK,
        ARMS_UP,
        ARCHER,
        DANGER,
        PLENTY,
        SHEAF,
        SHELTER,
        BLADE,
        MINER,
        SKULL,
        HEART,
        PRIZE,
        FRIEND,
        ANGLER,
        HOWL,
        BURN
    }
}
