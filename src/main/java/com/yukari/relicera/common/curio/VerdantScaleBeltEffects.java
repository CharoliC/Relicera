package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public final class VerdantScaleBeltEffects {
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_ID =
            UUID.fromString("a967dd46-a42b-4aa4-bf17-08ef954cb016");
    private static final String ARMOR_TOUGHNESS_MODIFIER_NAME = "Relicera Verdant Scale Belt armor toughness";
    private static final int REGENERATION_INTERVAL_TICKS = 50;
    private static final int REGENERATION_DURATION_TICKS = 100;
    private static final ResourceLocation NAGA_CHESTPLATE =
            ResourceLocation.fromNamespaceAndPath("twilightforest", "naga_chestplate");
    private static final ResourceLocation NAGA_LEGGINGS =
            ResourceLocation.fromNamespaceAndPath("twilightforest", "naga_leggings");

    private VerdantScaleBeltEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return ModItems.VERDANT_SCALE_BELT.isPresent()
                && CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.VERDANT_SCALE_BELT.get()))
                .orElse(false);
    }

    public static boolean shouldPassThrough(BlockState state, BlockPos pos, CollisionContext context,
                                            VoxelShape collisionShape) {
        return !collisionShape.isEmpty()
                && state.is(BlockTags.LEAVES)
                && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof LivingEntity entity
                && !context.isAbove(collisionShape, pos, false)
                && isEquipped(entity);
    }

    public static void tick(LivingEntity entity) {
        if (entity.level().isClientSide() || !ModItems.VERDANT_SCALE_BELT.isPresent()) {
            return;
        }

        boolean equipped = isEquipped(entity);
        updateArmorToughness(entity, equipped ? ModCommonConfig.VERDANT_SCALE_BELT_ARMOR_TOUGHNESS.get() : 0.0D);
        if (equipped && entity.isAlive() && isInsideLeaves(entity)) {
            refreshRegeneration(entity);
        }
    }

    private static void updateArmorToughness(LivingEntity entity, double amount) {
        AttributeInstance attribute = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (attribute == null) {
            return;
        }

        AttributeModifier existing = attribute.getModifier(ARMOR_TOUGHNESS_MODIFIER_ID);
        if (amount <= 0.0D) {
            if (existing != null) {
                attribute.removeModifier(ARMOR_TOUGHNESS_MODIFIER_ID);
            }
            return;
        }

        if (existing != null && existing.getOperation() == AttributeModifier.Operation.ADDITION
                && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            attribute.removeModifier(ARMOR_TOUGHNESS_MODIFIER_ID);
        }
        attribute.addTransientModifier(new AttributeModifier(ARMOR_TOUGHNESS_MODIFIER_ID,
                ARMOR_TOUGHNESS_MODIFIER_NAME, amount, AttributeModifier.Operation.ADDITION));
    }

    private static boolean isInsideLeaves(LivingEntity entity) {
        AABB bounds = entity.getBoundingBox().deflate(1.0E-7D);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            if (entity.level().hasChunkAt(pos) && entity.level().getBlockState(pos).is(BlockTags.LEAVES)) {
                return true;
            }
        }
        return false;
    }

    private static void refreshRegeneration(LivingEntity entity) {
        int amplifier = hasNagaArmor(entity) ? 1 : 0;
        MobEffectInstance existing = entity.getEffect(MobEffects.REGENERATION);
        if (existing == null) {
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_DURATION_TICKS, amplifier));
            return;
        }
        if (existing.getAmplifier() < amplifier) {
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_DURATION_TICKS, amplifier,
                    existing.isAmbient(), existing.isVisible(), existing.showIcon()));
            return;
        }
        if (existing.getAmplifier() != amplifier || existing.isInfiniteDuration()) {
            return;
        }

        int interval = REGENERATION_INTERVAL_TICKS >> amplifier;
        int extension = (REGENERATION_DURATION_TICKS - existing.getDuration()) / interval * interval;
        if (extension > 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, existing.getDuration() + extension, amplifier,
                    existing.isAmbient(), existing.isVisible(), existing.showIcon()));
        }
    }

    private static boolean hasNagaArmor(LivingEntity entity) {
        return NAGA_CHESTPLATE.equals(ForgeRegistries.ITEMS.getKey(entity.getItemBySlot(EquipmentSlot.CHEST).getItem()))
                && NAGA_LEGGINGS.equals(ForgeRegistries.ITEMS.getKey(entity.getItemBySlot(EquipmentSlot.LEGS).getItem()));
    }
}
