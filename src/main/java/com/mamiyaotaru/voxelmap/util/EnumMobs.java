package com.mamiyaotaru.voxelmap.util;

import net.minecraft.util.Identifier;

public enum EnumMobs {
    BLANK("Blank", false, 2, "textures/entity/bat.png", "", false, false),
    GENERICHOSTILE("Monster", false, 8, "textures/entity/zombie/zombie.png", "", true, false),
    GENERICNEUTRAL("Mob", false, 8, "textures/entity/pig/pig.png", "", false, true),
    GENERICTAME("Unknown Tame", false, 8, "textures/entity/wolf/wolf.png", "", false, true),
    BAT("Bat", true, 4, "textures/entity/bat.png", "", false, true),
    BLAZE("Blaze", true, 8, "textures/entity/blaze.png", "", true, false),
    CAT("Cat", true, 5, "textures/entity/cat/siamese.png", "", false, true),
    CAVESPIDER("Cave_Spider", true, 8, "textures/entity/spider/cave_spider.png", "", true, false),
    CHICKEN("Chicken", true, 6, "textures/entity/chicken.png", "", false, true),
    COD("Cod", true, 8, "textures/entity/fish/cod.png", "", false, true),
    COW("Cow", true, 10, "textures/entity/cow/cow.png", "", false, true),
    CREEPER("Creeper", true, 8, "textures/entity/creeper/creeper.png", "", true, false),
    DOLPHIN("Dolphin", true, 10, "textures/entity/dolphin.png", "", false, true),
    DROWNED("Drowned", true, 8, "textures/entity/zombie/drowned.png", "textures/entity/zombie/drowned_outer_layer.png", true, false),
    ENDERDRAGON("Ender_Dragon", true, 16, "textures/entity/enderdragon/dragon.png", "", true, false),
    ENDERMAN("Enderman", true, 8, "textures/entity/enderman/enderman.png", "", true, false),
    ENDERMITE("Endermite", true, 4, "textures/entity/endermite.png", "", true, false),
    EVOKER("Evoker", true, 8, "textures/entity/illager/evoker.png", "", true, false),
    GHAST("Ghast", true, 16, "textures/entity/ghast/ghast.png", "", true, false),
    GHASTATTACKING("Ghast", false, 16, "textures/entity/ghast/ghast_shooting.png", "", true, false),
    GUARDIAN("Guardian", true, 6, "textures/entity/guardian.png", "", true, false),
    GUARDIANELDER("Elder_Guardian", true, 12, "textures/entity/guardian_elder.png", "", true, false),
    HORSE("Horse", true, 8, "textures/entity/horse/horse_creamy.png", "", false, true),
    HUSK("Husk", true, 8, "textures/entity/zombie/husk.png", "", true, false),
    ILLUSIONER("Illusioner", true, 8, "textures/entity/illager/illusioner.png", "", true, false),
    IRONGOLEM("Iron_Golem", true, 8, "textures/entity/iron_golem.png", "", false, true),
    LLAMA("Llama", true, 8, "textures/entity/llama/brown.png", "", false, true),
    LLAMATRADER("Trader_Llama", true, 8, "textures/entity/llama/brown.png", "", false, true),
    MAGMA("Magma_Cube", true, 8, "textures/entity/slime/magmacube.png", "", true, false),
    MOOSHROOM("Mooshroom", true, 40, "textures/entity/cow/red_mooshroom.png", "", false, true),
    OCELOT("Ocelot", true, 5, "textures/entity/cat/ocelot.png", "", false, true),
    PARROT("Parrot", true, 8, "textures/entity/parrot/parrot_red_blue.png", "", false, true),
    PHANTOM("Phantom", true, 10, "textures/entity/phantom.png", "", true, false),
    PIG("Pig", true, 8, "textures/entity/pig/pig.png", "", false, true),
    PIGZOMBIE("Zombie_Pigman", true, 8, "textures/entity/zombie_pigman.png", "", true, true),
    PLAYER("Player", false, 8, "textures/entity/steve.png", "", false, false),
    POLARBEAR("Polar_Bear", true, 9, "textures/entity/bear/polarbear.png", "", true, true),
    RABBIT("Rabbit", true, 5, "textures/entity/rabbit/salt.png", "", false, true),
    PUFFERFISH("Pufferfish", true, 3, "textures/entity/fish/pufferfish.png", "", false, true),
    PUFFERFISHHALF("Pufferfish_Half", false, 5, "textures/entity/fish/pufferfish.png", "", false, true),
    PUFFERFISHFULL("Pufferfish_Full", false, 8, "textures/entity/fish/pufferfish.png", "", false, true),
    SALMON("Salmon", true, 13, "textures/entity/fish/salmon.png", "", false, true),
    SHEEP("Sheep", true, 6, "textures/entity/sheep/sheep.png", "", false, true),
    SHULKER("Shulker", true, 6, "textures/entity/shulker/shulker_purple.png", "", true, false),
    SILVERFISH("Silverfish", true, 6, "textures/entity/silverfish.png", "", true, false),
    SKELETON("Skeleton", true, 8, "textures/entity/skeleton/skeleton.png", "", true, false),
    SKELETONWITHER("Wither_Skeleton", true, 8, "textures/entity/skeleton/wither_skeleton.png", "", true, false),
    SLIME("Slime", true, 8, "textures/entity/slime/slime.png", "", true, false),
    SNOWGOLEM("Snow_Golem", true, 8, "textures/entity/snow_golem.png", "", false, true),
    SPIDER("Spider", true, 8, "textures/entity/spider/spider.png", "", true, false),
    SQUID("Squid", true, 6, "textures/entity/squid.png", "", false, true),
    STRAY("Stray", true, 8, "textures/entity/skeleton/stray.png", "textures/entity/skeleton/stray_overlay.png", true, false),
    TROPICALFISHA("Tropical_Fish", true, 5, "textures/entity/fish/tropical_a.png", "textures/entity/fish/tropical_a_pattern_1.png", false, true),
    TROPICALFISHB("Tropical_Fish", false, 6, "textures/entity/fish/tropical_b.png", "textures/entity/fish/tropical_b_pattern_4.png", false, true),
    TURTLE("Turtle", true, 6, "textures/entity/turtle/big_sea_turtle.png", "", false, true),
    VEX("Vex", true, 8, "textures/entity/illager/vex.png", "", true, false),
    VEXCHARGING("Vex", false, 8, "textures/entity/illager/vex_charging.png", "", true, false),
    VILLAGER("Villager", true, 8, "textures/entity/villager/villager.png", "", false, true),
    VINDICATOR("Vindicator", true, 8, "textures/entity/illager/vindicator.png", "", true, false),
    WITCH("Witch", true, 10, "textures/entity/witch.png", "", true, false),
    WITHER("Wither", true, 24, "textures/entity/wither/wither.png", "", true, false),
    WITHERINVULNERABLE("Wither", false, 24, "textures/entity/wither/wither_invulnerable.png", "", true, false),
    WOLF("Wolf", true, 6, "textures/entity/wolf/wolf.png", "", true, true),
    WOLFANGRY("Wolf", false, 6, "textures/entity/wolf/wolf_angry.png", "", true, false),
    WOLFTAME("Wolf", false, 6, "textures/entity/wolf/wolf_tame.png", "", false, true),
    ZOMBIE("Zombie", true, 8, "textures/entity/zombie/zombie.png", "", true, false),
    ZOMBIEVILLAGER("Zombie_villager", true, 8, "textures/entity/zombie_villager/zombie_villager.png", "", true, false),
    UNKNOWN("Unknown", false, 8, "/mob/uknown.png", "", true, true),
    CUSTOM("Custom", false, 8, "/mob/unknown.png", "", true, true),
    AUTO("Auto", false, 8, "/mob/unknown.png", "", true, true);

    public final String id;
    public final boolean isTopLevelUnit;
    public final int expectedWidth;
    public final Identifier resourceLocation;
    public final boolean isHostile;
    public final boolean isNeutral;
    public Identifier secondaryResourceLocation;
    public boolean enabled;

    EnumMobs(String name, boolean topLevelUnit, int expectedWidth, String path, String secondaryPath, boolean isHostile, boolean isNeutral) {
        this.id = name;
        this.isTopLevelUnit = topLevelUnit;
        this.expectedWidth = expectedWidth;
        this.resourceLocation = new Identifier(path.toLowerCase());
        this.secondaryResourceLocation = secondaryPath.equals("") ? null : new Identifier(secondaryPath.toLowerCase());
        this.isHostile = isHostile;
        this.isNeutral = isNeutral;
        this.enabled = true;
    }

    public static EnumMobs getMobByName(String par0) {
        for (EnumMobs enumMob : values()) {
            if (enumMob.id.equals(par0)) {
                return enumMob;
            }
        }

        return null;
    }

    public int returnEnumOrdinal() {
        return this.ordinal();
    }
}
