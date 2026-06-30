package com.yukari.relicera.common.astral;

import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class AstralLensDamageProtection {
    private static final float ENDERMAN_DAMAGE_MULTIPLIER = 0.15F;
    private static final float MAGIC_DAMAGE_MULTIPLIER = 0.70F;

    private AstralLensDamageProtection() {
    }

    public static void apply(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || !hasAstralLensInEnderChest(player)) {
            return;
        }

        DamageSource source = event.getSource();
        if (isEndermanDamage(source)) {
            event.setAmount(event.getAmount() * ENDERMAN_DAMAGE_MULTIPLIER);
        } else if (isMagicDamage(source)) {
            event.setAmount(event.getAmount() * MAGIC_DAMAGE_MULTIPLIER);
        }
    }

    private static boolean hasAstralLensInEnderChest(Player player) {
        for (int slot = 0; slot < player.getEnderChestInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getEnderChestInventory().getItem(slot);
            if (stack.is(ModItems.ASTRAL_LENS.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEndermanDamage(DamageSource source) {
        return isEnderman(source.getEntity()) || isEnderman(source.getDirectEntity());
    }

    private static boolean isEnderman(Entity entity) {
        return entity instanceof EnderMan;
    }

    private static boolean isMagicDamage(DamageSource source) {
        return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
    }
}
