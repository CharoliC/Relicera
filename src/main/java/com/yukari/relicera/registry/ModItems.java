package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.item.AstralLensItem;
import com.yukari.relicera.common.item.AshenTouchItem;
import com.yukari.relicera.common.item.BrutalPlunderBadgeItem;
import com.yukari.relicera.common.item.DivineSeveranceRingItem;
import com.yukari.relicera.common.item.EphemeralBloomItem;
import com.yukari.relicera.common.item.EphemeralBloomPendantItem;
import com.yukari.relicera.common.item.ForgottenThreadItem;
import com.yukari.relicera.common.item.FourfoldSherdPendantItem;
import com.yukari.relicera.common.item.IluthiasChaliceItem;
import com.yukari.relicera.common.item.NightGlovesItem;
import com.yukari.relicera.common.item.RelicCurioItem;
import com.yukari.relicera.common.item.RevivalNectarItem;
import com.yukari.relicera.common.item.SolarEmberItem;
import com.yukari.relicera.common.item.TempestsReinsItem;
import com.yukari.relicera.common.item.StriderSpursItem;
import com.yukari.relicera.common.item.TorchflowerEmberItem;
import com.yukari.relicera.common.item.WarfireFragmentItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ReliceraMod.MOD_ID);

    public static final RegistryObject<Item> FEYSILVER_INGOT = ITEMS.register("feysilver_ingot", () ->
            new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> FORGOTTEN_THREAD = ITEMS.register("forgotten_thread", () ->
            new ForgottenThreadItem(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ROTTEN_TUSK = ITEMS.register("rotten_tusk", () ->
            new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> RAW_HALLOWED_ENAMEL = ITEMS.register("raw_hallowed_enamel", () ->
            new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> HALLOWED_ENAMEL = ITEMS.register("hallowed_enamel", () ->
            new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> STORMSCALE = ITEMS.register("stormscale", () ->
            new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> RIPPLEHEART_PEARL = ITEMS.register("rippleheart_pearl", () ->
            new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> TEMPESTS_REINS = ITEMS.register("tempests_reins", () ->
            new TempestsReinsItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> CHALICE_LINING = ITEMS.register("chalice_lining", () ->
            new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> TORCHFLOWER_EMBER = ITEMS.register("torchflower_ember", () ->
            new TorchflowerEmberItem(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SOLAR_EMBER = ITEMS.register("solar_ember", () ->
            new SolarEmberItem(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> EPHEMERAL_BLOOM = ITEMS.register("ephemeral_bloom", () ->
            new EphemeralBloomItem(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> WARFIRE_FRAGMENT = ITEMS.register("warfire_fragment", () ->
            new WarfireFragmentItem(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)
                    .fireResistant()));

    public static final RegistryObject<Item> ASTRAL_LENS = ITEMS.register("astral_lens", () ->
            new AstralLensItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()));

    public static final RegistryObject<Item> REVIVAL_NECTAR = ITEMS.register("revival_nectar", () ->
            new RevivalNectarItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .food(RevivalNectarItem.createFoodProperties())));

    public static final RegistryObject<Item> EPHEMERAL_BLOOM_PENDANT = ITEMS.register("ephemeral_bloom_pendant", () ->
            new EphemeralBloomPendantItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> FOURFOLD_SHERD_PENDANT = ITEMS.register("fourfold_sherd_pendant", () ->
            new FourfoldSherdPendantItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NIGHT_GLOVES = ITEMS.register("night_gloves", () ->
            new NightGlovesItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ASHEN_TOUCH = ITEMS.register("ashen_touch", () ->
            new AshenTouchItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> DIVINE_SEVERANCE_RING = ITEMS.register("divine_severance_ring", () ->
            new DivineSeveranceRingItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> BRUTAL_PLUNDER_BADGE = ITEMS.register("brutal_plunder_badge", () ->
            new BrutalPlunderBadgeItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> STRIDER_SPURS = ITEMS.register("strider_spurs", () ->
            new StriderSpursItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> EXTINGUISHED_SOLAR_FURNACE = ITEMS.register("extinguished_solar_furnace", () ->
            new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> WITHERED_LIFE_CHALICE = ITEMS.register("withered_life_chalice", () ->
            new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> DRIED_CROWN = ITEMS.register("dried_crown", () ->
            new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GRANBELLS_FURNACE = ITEMS.register("granbells_furnace", () ->
            new RelicCurioItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> ILUTHIAS_CHALICE = ITEMS.register("iluthias_chalice", () ->
            new IluthiasChaliceItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NEREIAS_CROWN = ITEMS.register("nereias_crown", () ->
            new RelicCurioItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> RELIC_REPAIR_TABLE = ITEMS.register("relic_repair_table", () ->
            new BlockItem(ModBlocks.RELIC_REPAIR_TABLE.get(), new Item.Properties()
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> DREAMCATCHER_BOX = ITEMS.register("dreamcatcher_box", () ->
            new BlockItem(ModBlocks.DREAMCATCHER_BOX.get(), new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)));

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
