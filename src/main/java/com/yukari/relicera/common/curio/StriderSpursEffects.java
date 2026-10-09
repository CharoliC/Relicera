package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class StriderSpursEffects {
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("d62e9dbe-c36a-41f6-a299-e1e637d1903a");
    private static final String SPEED_MODIFIER_NAME = "Relicera strider spurs speed bonus";

    private StriderSpursEffects() {
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.STRIDER_SPURS.get()))
                .orElse(false);
    }

    public static void tickStrider(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Strider strider)) {
            return;
        }

        AttributeInstance movementSpeed = strider.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        if (!shouldBoost(strider)) {
            removeModifier(movementSpeed);
            return;
        }

        double speedBonus = ModCommonConfig.STRIDER_SPURS_SPEED_BONUS.get();
        if (speedBonus <= 0.0D) {
            removeModifier(movementSpeed);
            return;
        }

        AttributeModifier existing = movementSpeed.getModifier(SPEED_MODIFIER_ID);
        if (existing != null && Math.abs(existing.getAmount() - speedBonus) < 0.0001D) {
            return;
        }

        removeModifier(movementSpeed);
        movementSpeed.addTransientModifier(new AttributeModifier(
                SPEED_MODIFIER_ID,
                SPEED_MODIFIER_NAME,
                speedBonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    private static boolean shouldBoost(Strider strider) {
        Entity passenger = strider.getFirstPassenger();
        return passenger instanceof Player player
                && isEquipped(player)
                && (player.getMainHandItem().is(Items.WARPED_FUNGUS_ON_A_STICK)
                || player.getOffhandItem().is(Items.WARPED_FUNGUS_ON_A_STICK));
    }

    private static void removeModifier(AttributeInstance movementSpeed) {
        if (movementSpeed.getModifier(SPEED_MODIFIER_ID) != null) {
            movementSpeed.removeModifier(SPEED_MODIFIER_ID);
        }
    }
}
