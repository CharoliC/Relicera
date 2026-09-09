package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.item.armor.devilsbearskin.DevilsBearskinEffects;
import com.yukari.relicera.registry.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class DevilsBearskinClientEvents {
    private static final int SPAWN_INTERVAL = 12;

    private DevilsBearskinClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.isPaused()) {
            return;
        }

        for (Player player : level.players()) {
            if (!player.isAlive()
                    || (player.tickCount + player.getId()) % SPAWN_INTERVAL != 0
                    || !DevilsBearskinEffects.shouldEmitFlies(player)) {
                continue;
            }
            spawnFly(level, player);
        }
    }

    private static void spawnFly(ClientLevel level, Player player) {
        RandomSource random = level.random;
        double radius = 0.45D + random.nextDouble() * 0.35D;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double x = player.getX() + Math.cos(angle) * radius;
        double y = player.getY() + 0.25D + random.nextDouble() * Math.max(0.5D, player.getBbHeight() - 0.35D);
        double z = player.getZ() + Math.sin(angle) * radius;
        double xSpeed = (random.nextDouble() - 0.5D) * 0.025D;
        double ySpeed = (random.nextDouble() - 0.45D) * 0.012D;
        double zSpeed = (random.nextDouble() - 0.5D) * 0.025D;
        level.addParticle(ModParticleTypes.FLY.get(), x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
