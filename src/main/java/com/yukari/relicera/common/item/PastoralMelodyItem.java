package com.yukari.relicera.common.item;

import com.yukari.relicera.registry.ModSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

public class PastoralMelodyItem extends Item {
    public static final int USE_DURATION_TICKS = 140;
    private static final int GLOW_PARTICLE_COUNT = 36;

    public PastoralMelodyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        player.startUsingItem(hand);
        level.playSound(player, player, ModSoundEvents.PASTORAL_MELODY_PLAY.get(), SoundSource.RECORDS, 16.0F, 1.0F);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
        player.getCooldowns().addCooldown(this, USE_DURATION_TICKS);
        player.getCooldowns().addCooldown(Items.GOAT_HORN, USE_DURATION_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 0.08D, player.getZ(),
                    GLOW_PARTICLE_COUNT, 0.9D, 0.08D, 0.9D, 0.02D);
            PastoralMelodyEffects.charmNearbyAnimals(serverLevel, player);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.relicera.pastoral_melody.0").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
