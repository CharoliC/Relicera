package com.yukari.relicera.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class ModCommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue FORGOTTEN_THREAD_VILLAGE_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FORGOTTEN_THREAD_ANCIENT_CITY_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FORGOTTEN_THREAD_CAT_GIFT_CHANCE;
    public static final ForgeConfigSpec.IntValue REVIVAL_NECTAR_ILUTHIAS_BLESSING_DURATION;
    public static final ForgeConfigSpec.IntValue ILUTHIAS_BLESSING_MAX_ABSORPTION;
    public static final ForgeConfigSpec.DoubleValue EPHEMERAL_BLOOM_SNIFFER_DIGGING_NEW_MOON_CHANCE;
    public static final ForgeConfigSpec.DoubleValue EPHEMERAL_BLOOM_STRONGHOLD_LIBRARY_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue NIGHT_GLOVES_NIGHT_ATTACK_DAMAGE_BONUS;
    public static final ForgeConfigSpec.DoubleValue WARFIRE_FRAGMENT_IRON_GOLEM_DROP_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WARFIRE_FRAGMENT_IRON_GOLEM_KILLER_ENTITY_TYPES;
    public static final ForgeConfigSpec.DoubleValue WARFIRE_FRAGMENT_ALLAY_AURA_RANGE;
    public static final ForgeConfigSpec.DoubleValue ROTTEN_TUSK_PIGLIN_REPEL_RANGE;
    public static final ForgeConfigSpec.DoubleValue FEYSILVER_INGOT_OVERWORLD_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue STORMSCALE_ELDER_GUARDIAN_THUNDERSTORM_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue RIPPLEHEART_PEARL_AXOLOTL_ASSIST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue RIPPLEHEART_PEARL_WARM_OCEAN_RUIN_ARCHAEOLOGY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PASTORAL_MELODY_ANIMAL_RANGE;
    public static final ForgeConfigSpec.DoubleValue EXTINGUISHED_SOLAR_FURNACE_BASTION_TREASURE_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue EXTINGUISHED_SOLAR_FURNACE_NETHER_FORTRESS_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue EXTINGUISHED_SOLAR_FURNACE_WEAPONSMITH_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WITHERED_LIFE_CHALICE_WOODLAND_MANSION_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WITHERED_LIFE_CHALICE_DESERT_PYRAMID_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WITHERED_LIFE_CHALICE_JUNGLE_TEMPLE_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue WITHERED_LIFE_CHALICE_SIMPLE_DUNGEON_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DRIED_CROWN_SHIPWRECK_TREASURE_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DRIED_CROWN_UNDERWATER_RUIN_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DRIED_CROWN_BURIED_TREASURE_CHEST_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DRIED_CROWN_OCEAN_RUIN_ARCHAEOLOGY_CHANCE;
    public static final ForgeConfigSpec.IntValue BRUTAL_PLUNDER_BADGE_LOOTING_BONUS;
    public static final ForgeConfigSpec.DoubleValue BRUTAL_PLUNDER_BADGE_DAMAGE_BONUS_PER_LOOTING_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ASHEN_TOUCH_BURNING_TARGET_DAMAGE_BONUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIVINE_SEVERANCE_RING_HEAD_DROPS;
    public static final ForgeConfigSpec.DoubleValue STRIDER_SPURS_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue GRANBELLS_FURNACE_DAMAGE_BONUS;
    public static final ForgeConfigSpec.BooleanValue GRANBELLS_FURNACE_PRESERVE_SMITHING_TEMPLATES;
    public static final ForgeConfigSpec.BooleanValue GRANBELLS_FURNACE_KEEP_INVENTORY_IN_FIRE_OR_LAVA;
    public static final ForgeConfigSpec.DoubleValue ILUTHIAS_CHALICE_UNDEAD_DAMAGE_BONUS;
    public static final ForgeConfigSpec.DoubleValue ILUTHIAS_CHALICE_UNDEAD_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue NEREIAS_CROWN_ACTIVE_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue NEREIAS_CROWN_AQUATIC_AURA_RANGE;
    public static final ForgeConfigSpec.DoubleValue NEREIAS_CROWN_AQUATIC_ATTACK_DAMAGE_SHARE;
    public static final ForgeConfigSpec.DoubleValue NEREIAS_CROWN_AQUATIC_MAX_HEALTH_ARMOR_SHARE;
    public static final ForgeConfigSpec.DoubleValue DREAMCATCHER_BOX_SLEEP_RANGE;
    public static final ForgeConfigSpec.DoubleValue TEMPEST_SPRINT_SPEED_BONUS;
    public static final ForgeConfigSpec.DoubleValue TEMPEST_SPRINT_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue TEMPEST_SPRINT_COLLISION_KNOCKBACK;
    public static final ForgeConfigSpec.DoubleValue TEMPEST_SPRINT_COLLISION_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_RANGE;
    public static final ForgeConfigSpec.DoubleValue TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_KNOCKBACK;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("forgotten_thread");

        FORGOTTEN_THREAD_VILLAGE_CHEST_CHANCE = BUILDER
                .comment("Chance for Forgotten Thread to be added to village chest loot. 0.04 = 4%.")
                .defineInRange("villageChestChance", 0.06D, 0.0D, 1.0D);

        FORGOTTEN_THREAD_ANCIENT_CITY_CHEST_CHANCE = BUILDER
                .comment("Chance for Forgotten Thread to be added to ancient city chest loot. 0.03 = 3%.")
                .defineInRange("ancientCityChestChance", 0.03D, 0.0D, 1.0D);

        FORGOTTEN_THREAD_CAT_GIFT_CHANCE = BUILDER
                .comment("Chance for Forgotten Thread to be added to cat morning gifts. 0.20 = 20%.")
                .defineInRange("catGiftChance", 0.20D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("revival_nectar");

        REVIVAL_NECTAR_ILUTHIAS_BLESSING_DURATION = BUILDER
                .comment("Duration in ticks for Iluthia's Blessing granted by Revival Nectar. 400 ticks = 20 seconds.")
                .defineInRange("iluthiasBlessingDuration", 20 * 20, 1, 20 * 60 * 60);

        BUILDER.pop();

        BUILDER.push("iluthias_blessing");

        ILUTHIAS_BLESSING_MAX_ABSORPTION = BUILDER
                .comment("Maximum total absorption amount Iluthia's Blessing can accumulate. 60 = 30 absorption hearts. Existing absorption above this value is not reduced.")
                .defineInRange("maxAbsorption", 60, 0, 1024);

        BUILDER.pop();

        BUILDER.push("ephemeral_bloom");

        EPHEMERAL_BLOOM_SNIFFER_DIGGING_NEW_MOON_CHANCE = BUILDER
                .comment("Chance for Ephemeral Bloom to be added to sniffer digging loot during a new moon.")
                .defineInRange("snifferDiggingNewMoonChance", 0.17D, 0.0D, 1.0D);

        EPHEMERAL_BLOOM_STRONGHOLD_LIBRARY_CHEST_CHANCE = BUILDER
                .comment("Chance for Ephemeral Bloom to be added to stronghold library chest loot.")
                .defineInRange("strongholdLibraryChestChance", 0.52D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("night_gloves");

        NIGHT_GLOVES_NIGHT_ATTACK_DAMAGE_BONUS = BUILDER
                .comment("Melee attack damage bonus while Night Gloves are equipped at night. 0.20 = 20%.")
                .defineInRange("nightAttackDamageBonus", 0.20D, 0.0D, 10.0D);

        BUILDER.pop();

        BUILDER.push("warfire_fragment");

        WARFIRE_FRAGMENT_IRON_GOLEM_DROP_CHANCE = BUILDER
                .comment("Chance for Warfire Fragment to drop when an iron golem is killed during a raid by a configured entity type. 0.42 = 42%.")
                .defineInRange("ironGolemDropChance", 0.42D, 0.0D, 1.0D);

        WARFIRE_FRAGMENT_IRON_GOLEM_KILLER_ENTITY_TYPES = BUILDER
                .comment("Entity type ids that can trigger Warfire Fragment drops when killing an iron golem during a raid.")
                .defineList("ironGolemKillerEntityTypes", List.of(
                        "minecraft:ravager",
                        "minecraft:vindicator",
                        "minecraft:pillager",
                        "minecraft:evoker"
                ), ModCommonConfig::isValidResourceLocation);

        WARFIRE_FRAGMENT_ALLAY_AURA_RANGE = BUILDER
                .comment("Range in blocks for Allays holding a Warfire Fragment to grant Strength II and Resistance I.")
                .defineInRange("allayAuraRange", 24.0D, 0.0D, 128.0D);

        BUILDER.pop();

        BUILDER.push("rotten_tusk");

        ROTTEN_TUSK_PIGLIN_REPEL_RANGE = BUILDER
                .comment("Range in blocks for players holding a Rotten Tusk to repel ordinary Piglins.")
                .defineInRange("piglinRepelRange", 12.0D, 0.0D, 64.0D);

        BUILDER.pop();

        BUILDER.push("feysilver_ingot");

        FEYSILVER_INGOT_OVERWORLD_CHEST_CHANCE = BUILDER
                .comment("Chance for Feysilver Ingot to be added to selected overworld chest loot. 0.067 = 6.7%.")
                .defineInRange("overworldChestChance", 0.067D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("stormscale");

        STORMSCALE_ELDER_GUARDIAN_THUNDERSTORM_DROP_CHANCE = BUILDER
                .comment("Chance for Stormscale to drop when an Elder Guardian dies during a thunderstorm. 0.28 = 28%.")
                .defineInRange("elderGuardianThunderstormDropChance", 0.28D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("rippleheart_pearl");

        RIPPLEHEART_PEARL_AXOLOTL_ASSIST_CHANCE = BUILDER
                .comment("Chance for Rippleheart Pearl to drop when an Axolotl grants its assist Regeneration to a player. 0.06 = 6%.")
                .defineInRange("axolotlAssistChance", 0.06D, 0.0D, 1.0D);

        RIPPLEHEART_PEARL_WARM_OCEAN_RUIN_ARCHAEOLOGY_CHANCE = BUILDER
                .comment("Chance for Rippleheart Pearl to be added to warm ocean ruin archaeology loot. 0.036 = 3.6%.")
                .defineInRange("warmOceanRuinArchaeologyChance", 0.036D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("pastoral_melody");

        PASTORAL_MELODY_ANIMAL_RANGE = BUILDER
                .comment("Range in blocks for Pastoral Melody to put nearby adult animals into love mode.")
                .defineInRange("animalRange", 36.0D, 0.0D, 128.0D);

        BUILDER.pop();

        BUILDER.push("extinguished_solar_furnace");

        EXTINGUISHED_SOLAR_FURNACE_BASTION_TREASURE_CHEST_CHANCE = BUILDER
                .comment("Chance for Extinguished Solar Furnace to be added to bastion treasure chest loot. 0.37 = 37%.")
                .defineInRange("bastionTreasureChestChance", 0.37D, 0.0D, 1.0D);

        EXTINGUISHED_SOLAR_FURNACE_NETHER_FORTRESS_CHEST_CHANCE = BUILDER
                .comment("Chance for Extinguished Solar Furnace to be added to nether fortress chest loot. 0.055 = 5.5%.")
                .defineInRange("netherFortressChestChance", 0.055D, 0.0D, 1.0D);

        EXTINGUISHED_SOLAR_FURNACE_WEAPONSMITH_CHEST_CHANCE = BUILDER
                .comment("Chance for Extinguished Solar Furnace to be added to village weaponsmith chest loot. 0.031 = 3.1%.")
                .defineInRange("weaponsmithChestChance", 0.031D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("withered_life_chalice");

        WITHERED_LIFE_CHALICE_WOODLAND_MANSION_CHEST_CHANCE = BUILDER
                .comment("Chance for Withered Life Chalice to be added to each woodland mansion chest. 10.8%.")
                .defineInRange("woodlandMansionChestChance", 0.108D, 0.0D, 1.0D);

        WITHERED_LIFE_CHALICE_DESERT_PYRAMID_CHEST_CHANCE = BUILDER
                .comment("Chance for Withered Life Chalice to be added to each desert pyramid chest. 4.2%")
                .defineInRange("desertPyramidChestChance", 0.042D, 0.0D, 1.0D);

        WITHERED_LIFE_CHALICE_JUNGLE_TEMPLE_CHEST_CHANCE = BUILDER
                .comment("Chance for Withered Life Chalice to be added to each jungle temple chest. 10%")
                .defineInRange("jungleTempleChestChance", 0.10D, 0.0D, 1.0D);

        WITHERED_LIFE_CHALICE_SIMPLE_DUNGEON_CHEST_CHANCE = BUILDER
                .comment("Chance for Withered Life Chalice to be added to each dungeon chest. 1.6%")
                .defineInRange("simpleDungeonChestChance", 0.016D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("dried_crown");

        DRIED_CROWN_SHIPWRECK_TREASURE_CHEST_CHANCE = BUILDER
                .comment("Chance for Dried Crown to be added to shipwreck treasure chest loot. 0.05 = 5%.")
                .defineInRange("shipwreckTreasureChestChance", 0.05D, 0.0D, 1.0D);

        DRIED_CROWN_UNDERWATER_RUIN_CHEST_CHANCE = BUILDER
                .comment("Chance for Dried Crown to be added to big and small underwater ruin chest loot. 0.04 = 4%.")
                .defineInRange("underwaterRuinChestChance", 0.04D, 0.0D, 1.0D);

        DRIED_CROWN_BURIED_TREASURE_CHEST_CHANCE = BUILDER
                .comment("Chance for Dried Crown to be added to buried treasure chest loot. 0.08 = 8%.")
                .defineInRange("buriedTreasureChestChance", 0.08D, 0.0D, 1.0D);

        DRIED_CROWN_OCEAN_RUIN_ARCHAEOLOGY_CHANCE = BUILDER
                .comment("Chance for Dried Crown to be added to cold and warm ocean ruin archaeology loot. 0.024 = 2.4%.")
                .defineInRange("oceanRuinArchaeologyChance", 0.024D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("brutal_plunder_badge");

        BRUTAL_PLUNDER_BADGE_LOOTING_BONUS = BUILDER
                .comment("Looting level bonus while Brutal Plunder Badge is equipped.")
                .defineInRange("lootingBonus", 2, 0, 32);

        BRUTAL_PLUNDER_BADGE_DAMAGE_BONUS_PER_LOOTING_LEVEL = BUILDER
                .comment("Damage bonus per effective Looting level while Brutal Plunder Badge is equipped. 0.06D = 6%.")
                .defineInRange("damageBonusPerLootingLevel", 0.06D, 0.0D, 10.0D);

        BUILDER.pop();

        BUILDER.push("ashen_touch");

        ASHEN_TOUCH_BURNING_TARGET_DAMAGE_BONUS = BUILDER
                .comment("Damage bonus for melee attacks against targets that were already burning before being hit while Ashen Touch is equipped. 4.0 = +400%.")
                .defineInRange("burningTargetDamageBonus", 4.0D, 0.0D, 100.0D);

        BUILDER.pop();

        BUILDER.push("divine_severance_ring");

        DIVINE_SEVERANCE_RING_HEAD_DROPS = BUILDER
                .comment("Head drops for Ring of Judgment when the wearer kills a glowing configured undead entity. Format: entity_id|item_id")
                .defineList("headDrops", List.of(
                        "minecraft:zombie|minecraft:zombie_head",
                        "minecraft:zombie_villager|minecraft:zombie_head",
                        "minecraft:drowned|minecraft:zombie_head",
                        "minecraft:husk|minecraft:zombie_head",
                        "minecraft:skeleton|minecraft:skeleton_skull",
                        "minecraft:stray|minecraft:skeleton_skull",
                        "minecraft:wither_skeleton|minecraft:wither_skeleton_skull"
                ), ModCommonConfig::isValidHeadDropEntry);

        BUILDER.pop();

        BUILDER.push("strider_spurs");

        STRIDER_SPURS_SPEED_BONUS = BUILDER
                .comment("Movement speed bonus for ridden Striders while the rider has Strider Spurs equipped and holds a Warped Fungus on a Stick. 1.5 = +150%.")
                .defineInRange("speedBonus", 1.5D, 0.0D, 10.0D);

        BUILDER.pop();

        BUILDER.push("granbells_furnace");

        GRANBELLS_FURNACE_DAMAGE_BONUS = BUILDER
                .comment("Damage added by wearing Granbell's Furnace.")
                .defineInRange("damageBonus", 4.0D, 0.0D, 1024.0D);

        GRANBELLS_FURNACE_PRESERVE_SMITHING_TEMPLATES = BUILDER
                .comment("If true, smithing templates are restored when players wearing Granbell's Furnace take a smithing table result.")
                .define("preserveSmithingTemplates", true);

        GRANBELLS_FURNACE_KEEP_INVENTORY_IN_FIRE_OR_LAVA = BUILDER
                .comment("If true, players wearing Granbell's Furnace keep their inventory and curios when they die while in fire or lava.")
                .define("keepInventoryInFireOrLava", true);

        BUILDER.pop();

        BUILDER.push("iluthias_chalice");

        ILUTHIAS_CHALICE_UNDEAD_DAMAGE_BONUS = BUILDER
                .comment("Damage bonus dealt to undead creatures while Iluthia's Chalice is equipped. 0.30 = +30%.")
                .defineInRange("undeadDamageBonus", 0.30D, 0.0D, 10.0D);

        ILUTHIAS_CHALICE_UNDEAD_DAMAGE_REDUCTION = BUILDER
                .comment("Damage reduction against damage from undead creatures while Iluthia's Chalice is equipped. 0.30 = -30%.")
                .defineInRange("undeadDamageReduction", 0.30D, 0.0D, 1.0D);

        BUILDER.pop();

        BUILDER.push("nereias_crown");

        NEREIAS_CROWN_ACTIVE_DAMAGE_REDUCTION = BUILDER
                .comment("Damage reduction while Nereia's Crown is active in water or exposed rain. 0.40 = -40%.")
                .defineInRange("activeDamageReduction", 0.40D, 0.0D, 1.0D);

        NEREIAS_CROWN_AQUATIC_AURA_RANGE = BUILDER
                .comment("Range in blocks for Nereia's Crown to count nearby aquatic creatures for its attribute aura.")
                .defineInRange("aquaticAuraRange", 24.0D, 0.0D, 128.0D);

        NEREIAS_CROWN_AQUATIC_ATTACK_DAMAGE_SHARE = BUILDER
                .comment("Share of nearby aquatic creatures' total attack damage added to the wearer. 0.30 = 30%.")
                .defineInRange("aquaticAttackDamageShare", 0.30D, 0.0D, 10.0D);

        NEREIAS_CROWN_AQUATIC_MAX_HEALTH_ARMOR_SHARE = BUILDER
                .comment("Share of nearby aquatic creatures' total max health added as armor to the wearer. 0.10 = 10%.")
                .defineInRange("aquaticMaxHealthArmorShare", 0.10D, 0.0D, 10.0D);

        BUILDER.pop();

        BUILDER.push("dreamcatcher_box");

        DREAMCATCHER_BOX_SLEEP_RANGE = BUILDER
                .comment("Range in blocks for Dreamcatcher Boxes to react to nearby sleeping players and villagers.")
                .defineInRange("sleepRange", 8.0D, 0.0D, 64.0D);

        BUILDER.pop();

        BUILDER.push("tempest_sprint");

        TEMPEST_SPRINT_SPEED_BONUS = BUILDER
                .comment("Movement speed bonus for vanilla Horses under Tempest Sprint. 0.40 = +40%.")
                .defineInRange("speedBonus", 0.40D, 0.0D, 10.0D);

        TEMPEST_SPRINT_DAMAGE_REDUCTION = BUILDER
                .comment("Damage reduction for vanilla Horses under Tempest Sprint. 0.60 = -60%.")
                .defineInRange("damageReduction", 0.60D, 0.0D, 1.0D);

        TEMPEST_SPRINT_COLLISION_KNOCKBACK = BUILDER
                .comment("Base knockback to hostile mobs when colliding.")
                .defineInRange("collisionKnockback", 2.0D, 0.0D, 64.0D);

        TEMPEST_SPRINT_COLLISION_DAMAGE = BUILDER
                .comment("Base damage to hostile mobs when colliding.")
                .defineInRange("collisionDamage", 8.0D, 0.0D, 1024.0D);

        TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_RANGE = BUILDER
                .comment("Range of full jump shockwave.")
                .defineInRange("fullJumpShockwaveRange", 12.0D, 0.0D, 64.0D);

        TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_DAMAGE = BUILDER
                .comment("Damage applied when full jump under Tempest Sprint.")
                .defineInRange("fullJumpShockwaveDamage", 18.0D, 0.0D, 1024.0D);

        TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_KNOCKBACK = BUILDER
                .comment("Knockback applied when full jump under Tempest Sprint.")
                .defineInRange("fullJumpShockwaveKnockback", 2.5D, 0.0D, 64.0D);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ModCommonConfig() {
    }

    private static boolean isValidResourceLocation(Object value) {
        return value instanceof String string && ResourceLocation.isValidResourceLocation(string);
    }

    private static boolean isValidHeadDropEntry(Object value) {
        if (!(value instanceof String string)) {
            return false;
        }

        String[] parts = string.split("\\|");
        return parts.length == 2
                && ResourceLocation.isValidResourceLocation(parts[0].trim())
                && ResourceLocation.isValidResourceLocation(parts[1].trim());
    }
}
