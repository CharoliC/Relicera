package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.astral.AstralObservationClientData;
import com.yukari.relicera.client.tooltip.HoldShiftTooltip;
import com.yukari.relicera.common.curio.FourfoldSherdPendantEffects;
import com.yukari.relicera.config.ModServerConfig;
import com.yukari.relicera.registry.ModItems;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class ClientTooltipEvents {
    private ClientTooltipEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAstralLensTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(ModItems.ASTRAL_LENS.get())) {
            appendAstralLensTooltip(event);
        }

        if (event.getItemStack().is(ModItems.WARFIRE_FRAGMENT.get())) {
            appendWarfireFragmentTooltip(event);
        }

        if (event.getItemStack().is(ModItems.NIGHT_GLOVES.get())) {
            appendNightGlovesTooltip(event);
        }

        if (event.getItemStack().is(ModItems.ASHEN_TOUCH.get())) {
            appendAshenTouchTooltip(event);
        }

        if (event.getItemStack().is(ModItems.STRIDER_SPURS.get())) {
            appendStriderSpursTooltip(event);
        }

        if (event.getItemStack().is(ModItems.EPHEMERAL_BLOOM_PENDANT.get())) {
            appendEphemeralBloomPendantTooltip(event);
        }

        if (event.getItemStack().is(ModItems.BRUTAL_PLUNDER_BADGE.get())) {
            appendBrutalPlunderBadgeTooltip(event);
        }

        if (event.getItemStack().is(ModItems.FOURFOLD_SHERD_PENDANT.get())) {
            appendFourfoldSherdPendantTooltip(event);
        }

        if (event.getItemStack().is(ModItems.GRANBELLS_FURNACE.get())) {
            appendGranbellsFurnaceTooltip(event);
        }

        if (event.getItemStack().is(ModItems.ILUTHIAS_CHALICE.get())) {
            appendIluthiasChaliceTooltip(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpyglassTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(Items.SPYGLASS) || event.getEntity() == null) {
            return;
        }

        int observedCount = AstralObservationClientData.getObservedCount();
        if (observedCount <= 0 || AstralObservationClientData.hasClaimedAstralLens()) {
            return;
        }

        event.getToolTip().add(Component.translatable("tooltip.relicera.spyglass.observed_moon_phases")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" "))
                .append(Component.literal(observedCount + " / 8").withStyle(ChatFormatting.WHITE)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onReliceraTooltipOrder(ItemTooltipEvent event) {
        if (isReliceraItem(event.getItemStack())) {
            moveReliceraModNameToEnd(event.getToolTip());
        }
    }

    private static void appendAstralLensTooltip(ItemTooltipEvent event) {
        HoldShiftTooltip.append(
                event.getToolTip(),
                Component.translatable("item.relicera.astral_lens.tooltip.flavor"),
                Component.translatable("item.relicera.astral_lens.tooltip.hold_shift.before"),
                Component.literal("Shift"),
                Component.translatable("item.relicera.astral_lens.tooltip.hold_shift.after"),
                java.util.List.of(
                        HoldShiftTooltip.lightPurple(Component.translatable("item.relicera.astral_lens.tooltip.ender_chest")),
                        HoldShiftTooltip.goldPrefixLine("-85%", "item.relicera.astral_lens.tooltip.enderman_damage"),
                        HoldShiftTooltip.goldPrefixLine("-30%", "item.relicera.astral_lens.tooltip.magic_damage")
                )
        );
    }

    private static void appendNightGlovesTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.night_gloves.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.night_gloves.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(Component.literal(formatNightGlovesDamageBonus())
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("item.relicera.night_gloves.tooltip.night_melee_damage")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.night_gloves.tooltip.highlight.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.night_gloves.tooltip.sculk_shrieker")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.night_gloves.tooltip.highlight.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.night_gloves.tooltip.silent_container.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.night_gloves.tooltip.sculk_sensor")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.night_gloves.tooltip.silent_container.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static void appendAshenTouchTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.ashen_touch.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.ashen_touch.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.ashen_touch.tooltip.prevent_ignition")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.ashen_touch.tooltip.burning_target.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(formatAshenTouchDamageBonus())
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.ashen_touch.tooltip.burning_target.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static void appendStriderSpursTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.strider_spurs.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.strider_spurs.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.strider_spurs.tooltip.speed.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(formatStriderSpursSpeedBonus())
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.strider_spurs.tooltip.speed.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static void appendEphemeralBloomPendantTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.ephemeral_bloom_pendant.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.ephemeral_bloom_pendant.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.ephemeral_bloom_pendant.tooltip.absorption.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.ephemeral_bloom_pendant.tooltip.absorption")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.ephemeral_bloom_pendant.tooltip.absorption.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static void appendWarfireFragmentTooltip(ItemTooltipEvent event) {
        HoldShiftTooltip.append(
                event.getToolTip(),
                Component.translatable("item.relicera.warfire_fragment.tooltip.flavor"),
                Component.translatable("item.relicera.warfire_fragment.tooltip.hold_shift.before"),
                Component.literal("Shift"),
                Component.translatable("item.relicera.warfire_fragment.tooltip.hold_shift.after"),
                java.util.List.of(
                        HoldShiftTooltip.lightPurple(Component.translatable("item.relicera.warfire_fragment.tooltip.allay_holding")),
                        darkPurpleBullet()
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.aura.before")
                                        .withStyle(ChatFormatting.DARK_PURPLE))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.player").withStyle(ChatFormatting.GOLD))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.aura.middle").withStyle(ChatFormatting.DARK_PURPLE))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.friendly_units").withStyle(ChatFormatting.GOLD))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.aura.after").withStyle(ChatFormatting.DARK_PURPLE)),
                        darkPurpleBullet()
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.damage_immunity.before")
                                        .withStyle(ChatFormatting.DARK_PURPLE))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.player").withStyle(ChatFormatting.GOLD))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.damage_immunity.middle").withStyle(ChatFormatting.DARK_PURPLE))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.friendly_units").withStyle(ChatFormatting.GOLD))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.damage_immunity.after").withStyle(ChatFormatting.DARK_PURPLE)),
                        darkPurpleBullet()
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.fire_immunity.before")
                                        .withStyle(ChatFormatting.DARK_PURPLE))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.fire").withStyle(ChatFormatting.GOLD))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.fire_immunity.middle").withStyle(ChatFormatting.DARK_PURPLE))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.lava").withStyle(ChatFormatting.GOLD))
                                .append(Component.translatable("item.relicera.warfire_fragment.tooltip.fire_immunity.after").withStyle(ChatFormatting.DARK_PURPLE))
                )
        );
    }

    private static void appendBrutalPlunderBadgeTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(Component.literal(formatSignedNumber(com.yukari.relicera.config.ModServerConfig.BRUTAL_PLUNDER_BADGE_LOOTING_BONUS.get()))
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.looting_level")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.piglin")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.piglin_flee")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.piglin_barter.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.piglin")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.piglin_barter.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.damage_by_looting")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.current_effect")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        event.getToolTip().add(Component.literal(formatDamageBonus(event))
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("item.relicera.brutal_plunder_badge.tooltip.attack_damage")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static void appendFourfoldSherdPendantTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.insert.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.insert.sherds")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.insert.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));

        List<ItemStack> sherds = FourfoldSherdPendantEffects.getSherdsInStack(event.getItemStack());
        if (sherds.isEmpty()) {
            return;
        }

        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.current_effect")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        for (ItemStack sherd : sherds) {
            event.getToolTip().add(createFourfoldSherdEffectLine(sherd));
        }
    }

    private static Component createFourfoldSherdEffectLine(ItemStack sherd) {
        FourfoldSherdPendantEffects.SherdPattern pattern = FourfoldSherdPendantEffects.getPattern(sherd);
        if (pattern == null) {
            return darkPurpleBullet()
                    .append(Component.literal("[")
                            .withStyle(ChatFormatting.GRAY))
                    .append(sherd.getHoverName().copy()
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("] ")
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.effect.unknown")
                            .withStyle(ChatFormatting.GRAY));
        }

        return switch (pattern) {
            case SNORT -> fourfoldEffectLine("snort", "+1");
            case BREWER -> fourfoldEffectLine("brewer", "+20%");
            case MOURNER -> fourfoldEffectLine("mourner", "+50%");
            case EXPLORER -> fourfoldEffectLine("explorer", "+20%");
            case HEARTBREAK -> fourfoldEffectLine("heartbreak", "+25%");
            case ARMS_UP -> fourfoldEffectLine("arms_up", "+1");
            case ARCHER -> fourfoldEffectLine("archer", "+20%");
            case DANGER -> fourfoldEffectLine("danger", "-50%");
            case PLENTY -> fourfoldEffectLine("plenty", "+1");
            case SHEAF -> fourfoldEffectLine("sheaf", "+1");
            case SHELTER -> fourfoldEffectLine("shelter", "+4");
            case BLADE -> fourfoldEffectLine("blade", "+10%");
            case MINER -> fourfoldEffectLine("miner", "+10%");
            case SKULL -> fourfoldEffectLine("skull", "+15%");
            case HEART -> fourfoldEffectLine("heart", "+4");
            case PRIZE -> fourfoldEffectLine("prize", "+1");
            case FRIEND -> fourfoldEffectLine("friend", "+1");
            case ANGLER -> fourfoldEffectLine("angler", "+1");
            case HOWL -> fourfoldEffectLine("howl", "+20%");
            case BURN -> fourfoldEffectLine("burn", "+20%");
        };
    }

    private static Component fourfoldEffectLine(String patternKey, String value) {
        return darkPurpleBullet()
                .append(Component.literal("[")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.pattern." + patternKey)
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal("] ")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.effect." + patternKey + ".before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(value)
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.fourfold_sherd_pendant.tooltip.effect." + patternKey + ".after")
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static void appendGranbellsFurnaceTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.translatable("item.relicera.granbells_furnace.tooltip.flavor")
                .withStyle(ChatFormatting.DARK_RED));
        event.getToolTip().add(Component.empty());

        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.granbells_furnace.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.granbells_furnace.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(Component.literal(formatGranbellsFurnaceDamageBonus())
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.attack_damage")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.fire_lava_immunity.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.fire")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.fire_lava_immunity.middle")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.lava")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.fire_lava_immunity.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.lava_movement")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.ignite_attackers")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.anvil.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.anvil")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.anvil.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.smithing_template.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.smithing_template")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.smithing_template.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.keep_inventory.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.fire")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.keep_inventory.middle")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.lava")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.granbells_furnace.tooltip.keep_inventory.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static void appendIluthiasChaliceTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.translatable("item.relicera.iluthias_chalice.tooltip.flavor")
                .withStyle(ChatFormatting.DARK_RED));
        event.getToolTip().add(Component.empty());

        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(Component.translatable("item.relicera.iluthias_chalice.tooltip.hold_shift.before")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Shift").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.hold_shift.after")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            return;
        }

        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.immunity.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.hunger")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.immunity.middle_1")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.poison")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.immunity.middle_2")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.wither")
                        .withStyle(ChatFormatting.GOLD)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.regeneration")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.undead_damage.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(formatSignedPercent(ModServerConfig.ILUTHIAS_CHALICE_UNDEAD_DAMAGE_BONUS.get()))
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.undead_damage.middle")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(formatNegativePercent(ModServerConfig.ILUTHIAS_CHALICE_UNDEAD_DAMAGE_REDUCTION.get()))
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.undead_damage.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.clear_undead_effects")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.store_totems")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
        event.getToolTip().add(darkPurpleBullet()
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.bless_totems.before")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.iluthias_blessing")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.translatable("item.relicera.iluthias_chalice.tooltip.bless_totems.after")
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private static String formatSignedNumber(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private static String formatNightGlovesDamageBonus() {
        return "+" + Math.round(ModServerConfig.NIGHT_GLOVES_NIGHT_ATTACK_DAMAGE_BONUS.get() * 100.0D) + "%";
    }

    private static String formatAshenTouchDamageBonus() {
        return "+" + Math.round(ModServerConfig.ASHEN_TOUCH_BURNING_TARGET_DAMAGE_BONUS.get() * 100.0D) + "%";
    }

    private static String formatStriderSpursSpeedBonus() {
        return "+" + Math.round(ModServerConfig.STRIDER_SPURS_SPEED_BONUS.get() * 100.0D) + "%";
    }

    private static String formatGranbellsFurnaceDamageBonus() {
        double value = ModServerConfig.GRANBELLS_FURNACE_DAMAGE_BONUS.get();
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return formatSignedNumber((int) Math.rint(value));
        }
        return (value >= 0.0D ? "+" : "") + String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static String formatSignedPercent(double value) {
        long percent = Math.round(value * 100.0D);
        return (percent >= 0L ? "+" : "") + percent + "%";
    }

    private static String formatNegativePercent(double value) {
        long percent = Math.round(value * 100.0D);
        return "-" + percent + "%";
    }

    private static String formatDamageBonus(ItemTooltipEvent event) {
        int effectiveLootingLevel = getTooltipLootingLevel(event);

        long percent = Math.round(com.yukari.relicera.config.ModServerConfig.BRUTAL_PLUNDER_BADGE_DAMAGE_BONUS_PER_LOOTING_LEVEL.get()
                * effectiveLootingLevel
                * 100.0D);
        return "+" + percent + "%";
    }

    private static int getTooltipLootingLevel(ItemTooltipEvent event) {
        int badgeLootingBonus = com.yukari.relicera.config.ModServerConfig.BRUTAL_PLUNDER_BADGE_LOOTING_BONUS.get();
        Player player = event.getEntity();
        if (player == null) {
            return badgeLootingBonus;
        }

        int baseLooting = EnchantmentHelper.getMobLooting(player);
        int curiosLooting = CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.getLootingLevel(getTooltipDamageSource(player), player, baseLooting))
                .orElse(0);
        return badgeLootingBonus + baseLooting + curiosLooting;
    }

    private static DamageSource getTooltipDamageSource(Player player) {
        return player.damageSources().playerAttack(player);
    }

    private static MutableComponent darkPurpleBullet() {
        return Component.literal("- ").withStyle(ChatFormatting.DARK_PURPLE);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (!isReliceraItem(event)) {
            return;
        }

        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        for (int index = elements.size() - 1; index >= 0; index--) {
            Either<FormattedText, TooltipComponent> element = elements.get(index);
            if (element.left().map(FormattedText::getString).filter(ClientTooltipEvents::isReliceraModNameLine).isPresent()) {
                elements.remove(index);
                elements.add(element);
            }
        }
    }

    private static void moveReliceraModNameToEnd(List<Component> tooltip) {
        for (int index = tooltip.size() - 1; index >= 0; index--) {
            Component line = tooltip.get(index);
            if (isReliceraModNameLine(line.getString())) {
                tooltip.remove(index);
                tooltip.add(line);
            }
        }
    }

    private static boolean isReliceraModNameLine(String text) {
        return ReliceraMod.MOD_ID.equals(text)
                || "Relicera".equals(text)
                || text.contains(ReliceraMod.MOD_ID)
                || text.contains("Relicera");
    }

    private static boolean isReliceraItem(RenderTooltipEvent.GatherComponents event) {
        return isReliceraItem(event.getItemStack());
    }

    private static boolean isReliceraItem(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() != Items.AIR
                && ForgeRegistries.ITEMS.getKey(stack.getItem()) != null
                && ReliceraMod.MOD_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace());
    }
}
