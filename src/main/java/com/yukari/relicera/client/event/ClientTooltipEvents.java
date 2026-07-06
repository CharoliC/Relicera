package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.astral.AstralObservationClientData;
import com.yukari.relicera.common.curio.FourfoldSherdPendantEffects;
import com.yukari.relicera.config.ModServerConfig;
import com.yukari.relicera.registry.ModItems;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
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
        event.getToolTip().add(tooltipLine("astral_lens", 0));
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("astral_lens", 1));
            return;
        }

        event.getToolTip().add(tooltipLine("astral_lens", 2));
        event.getToolTip().add(tooltipLine("astral_lens", 3, gold("-85%")));
        event.getToolTip().add(tooltipLine("astral_lens", 4, gold("-30%")));
    }

    private static void appendNightGlovesTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("night_gloves"));
            return;
        }

        event.getToolTip().add(tooltipLine("night_gloves", 1, gold(formatNightGlovesDamageBonus())));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(tooltipLine("night_gloves", 2));
        event.getToolTip().add(tooltipLine("night_gloves", 3));
    }

    private static void appendAshenTouchTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("ashen_touch"));
            return;
        }

        event.getToolTip().add(tooltipLine("ashen_touch", 1));
        event.getToolTip().add(tooltipLine("ashen_touch", 2, gold(formatAshenTouchDamageBonus())));
    }

    private static void appendStriderSpursTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("strider_spurs"));
            return;
        }

        event.getToolTip().add(tooltipLine("strider_spurs", 1, gold(formatStriderSpursSpeedBonus())));
    }

    private static void appendEphemeralBloomPendantTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("ephemeral_bloom_pendant"));
            return;
        }

        event.getToolTip().add(tooltipLine("ephemeral_bloom_pendant", 1));
    }

    private static void appendWarfireFragmentTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(tooltipLine("warfire_fragment", 0));
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("warfire_fragment", 1));
            return;
        }

        event.getToolTip().add(tooltipLine("warfire_fragment", 2));
        event.getToolTip().add(tooltipLine("warfire_fragment", 3));
        event.getToolTip().add(tooltipLine("warfire_fragment", 4));
        event.getToolTip().add(tooltipLine("warfire_fragment", 5));
    }

    private static void appendBrutalPlunderBadgeTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("brutal_plunder_badge"));
            return;
        }

        event.getToolTip().add(tooltipLine("brutal_plunder_badge", 1,
                gold(formatSignedNumber(ModServerConfig.BRUTAL_PLUNDER_BADGE_LOOTING_BONUS.get()))));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(tooltipLine("brutal_plunder_badge", 2));
        event.getToolTip().add(tooltipLine("brutal_plunder_badge", 3));
        event.getToolTip().add(tooltipLine("brutal_plunder_badge", 4));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(tooltipLine("brutal_plunder_badge", 5));
        event.getToolTip().add(tooltipLine("brutal_plunder_badge", 6, gold(formatDamageBonus(event))));
    }

    private static void appendFourfoldSherdPendantTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("fourfold_sherd_pendant"));
            return;
        }

        event.getToolTip().add(tooltipLine("fourfold_sherd_pendant", 1));

        List<ItemStack> sherds = FourfoldSherdPendantEffects.getSherdsInStack(event.getItemStack());
        if (sherds.isEmpty()) {
            return;
        }

        event.getToolTip().add(Component.empty());
        event.getToolTip().add(tooltipLine("fourfold_sherd_pendant", 2));
        for (ItemStack sherd : sherds) {
            event.getToolTip().add(createFourfoldSherdEffectLine(sherd));
        }
    }

    private static Component createFourfoldSherdEffectLine(ItemStack sherd) {
        FourfoldSherdPendantEffects.SherdPattern pattern = FourfoldSherdPendantEffects.getPattern(sherd);
        if (pattern == null) {
            return tooltipLine("fourfold_sherd_pendant", 3, sherd.getHoverName().copy().withStyle(ChatFormatting.GRAY));
        }

        return switch (pattern) {
            case SNORT -> fourfoldEffectLine(4, 24, "+1");
            case BREWER -> fourfoldEffectLine(5, 25, "+20%");
            case MOURNER -> fourfoldEffectLine(6, 26, "+50%");
            case EXPLORER -> fourfoldEffectLine(7, 27, "+20%");
            case HEARTBREAK -> fourfoldEffectLine(8, 28, "+25%");
            case ARMS_UP -> fourfoldEffectLine(9, 29, "+1");
            case ARCHER -> fourfoldEffectLine(10, 30, "+20%");
            case DANGER -> fourfoldEffectLine(11, 31, "-50%");
            case PLENTY -> fourfoldEffectLine(12, 32, "+1");
            case SHEAF -> fourfoldEffectLine(13, 33, "+1");
            case SHELTER -> fourfoldEffectLine(14, 34, "+4");
            case BLADE -> fourfoldEffectLine(15, 35, "+10%");
            case MINER -> fourfoldEffectLine(16, 36, "+10%");
            case SKULL -> fourfoldEffectLine(17, 37, "+15%");
            case HEART -> fourfoldEffectLine(18, 38, "+4");
            case PRIZE -> fourfoldEffectLine(19, 39, "+1");
            case FRIEND -> fourfoldEffectLine(20, 40, "+1");
            case ANGLER -> fourfoldEffectLine(21, 41, "+1");
            case HOWL -> fourfoldEffectLine(22, 42, "+20%");
            case BURN -> fourfoldEffectLine(23, 43, "+20%");
        };
    }

    private static Component fourfoldEffectLine(int patternLine, int effectLine, String value) {
        return tooltipLine("fourfold_sherd_pendant", effectLine,
                tooltipLine("fourfold_sherd_pendant", patternLine).copy().withStyle(ChatFormatting.DARK_PURPLE),
                gold(value));
    }

    private static void appendGranbellsFurnaceTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(tooltipLine("granbells_furnace", 0));
        event.getToolTip().add(Component.empty());

        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("granbells_furnace", 1));
            return;
        }

        event.getToolTip().add(tooltipLine("granbells_furnace", 2, gold(formatGranbellsFurnaceDamageBonus())));
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(tooltipLine("granbells_furnace", 3));
        event.getToolTip().add(tooltipLine("granbells_furnace", 4));
        event.getToolTip().add(tooltipLine("granbells_furnace", 5));
        event.getToolTip().add(tooltipLine("granbells_furnace", 6));
        event.getToolTip().add(tooltipLine("granbells_furnace", 7));
        event.getToolTip().add(tooltipLine("granbells_furnace", 8));
    }

    private static void appendIluthiasChaliceTooltip(ItemTooltipEvent event) {
        event.getToolTip().add(Component.empty());
        event.getToolTip().add(tooltipLine("iluthias_chalice", 0));
        event.getToolTip().add(Component.empty());

        if (!Screen.hasShiftDown()) {
            event.getToolTip().add(holdShiftLine("iluthias_chalice", 1));
            return;
        }

        event.getToolTip().add(tooltipLine("iluthias_chalice", 2));
        event.getToolTip().add(tooltipLine("iluthias_chalice", 3));
        event.getToolTip().add(tooltipLine("iluthias_chalice", 4,
                gold(formatSignedPercent(ModServerConfig.ILUTHIAS_CHALICE_UNDEAD_DAMAGE_BONUS.get())),
                gold(formatNegativePercent(ModServerConfig.ILUTHIAS_CHALICE_UNDEAD_DAMAGE_REDUCTION.get()))));
        event.getToolTip().add(tooltipLine("iluthias_chalice", 5));
        event.getToolTip().add(tooltipLine("iluthias_chalice", 6));
        event.getToolTip().add(tooltipLine("iluthias_chalice", 7));
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

    private static Component holdShiftLine(String itemId) {
        return tooltipLine(itemId, 0, gold("Shift"));
    }

    private static Component holdShiftLine(String itemId, int line) {
        return tooltipLine(itemId, line, gold("Shift"));
    }

    private static Component tooltipLine(String itemId, int line, Object... args) {
        return Component.translatable("tooltip.relicera." + itemId + "." + line, args);
    }

    private static Component gold(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GOLD);
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
