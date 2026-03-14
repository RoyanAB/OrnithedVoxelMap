package com.mamiyaotaru.voxelmap.util;

import net.minecraft.util.ResourceLocation;

public enum EnumMobs {
    BLANK("Blank", "", false, 2, "textures/entity/bat.png", "", false, false),
    GENERICHOSTILE("Monster", "hostile", false, 8, "textures/entity/zombie/zombie.png", "", true, false),
    GENERICNEUTRAL("Mob", "neutral", false, 8, "textures/entity/pig/pig.png", "", false, true),
    GENERICTAME("Unknown Tame", "tame", false, 8, "textures/entity/wolf/wolf.png", "", false, true),
    BAT("Bat", "net.minecraft.entity.passive.EntityBat", true, 4, "textures/entity/bat.png", "", false, true),
    BLAZE("Blaze", "net.minecraft.entity.monster.EntityBlaze", true, 8, "textures/entity/blaze.png", "", true, false),
    CAT("Cat", "net.minecraft.entity.passive.EntityOcelot", true, 5, "textures/entity/cat/siamese.png", "", false, true),
    CAVESPIDER("Cave_Spider", "net.minecraft.entity.monster.EntityCaveSpider", true, 8, "textures/entity/spider/cave_spider.png", "", true, false),
    CHICKEN("Chicken", "net.minecraft.entity.passive.EntityChicken", true, 6, "textures/entity/chicken.png", "", false, true),
    COD("Cod", "net.minecraft.entity.passive.EntityCod", true, 8, "textures/entity/fish/cod.png", "", false, true),
    COW("Cow", "net.minecraft.entity.passive.EntityCow", true, 10, "textures/entity/cow/cow.png", "", false, true),
    CREEPER("Creeper", "net.minecraft.entity.monster.EntityCreeper", true, 8, "textures/entity/creeper/creeper.png", "", true, false),
    DOLPHIN("Dolphin", "net.minecraft.entity.passive.EntityDolphin", true, 10, "textures/entity/dolphin.png", "", false, true),
    DROWNED(
            "Drowned",
            "net.minecraft.entity.monster.EntityDrowned",
            true,
            8,
            "textures/entity/zombie/drowned.png",
            "textures/entity/zombie/drowned_outer_layer.png",
            true,
            false
    ),
    ENDERDRAGON("Ender_Dragon", "net.minecraft.entity.boss.EntityDragon", true, 16, "textures/entity/enderdragon/dragon.png", "", true, false),
    ENDERMAN("Enderman", "net.minecraft.entity.monster.EntityEnderman", true, 8, "textures/entity/enderman/enderman.png", "", true, false),
    ENDERMITE("Endermite", "net.minecraft.entity.monster.EntityEndermite", true, 4, "textures/entity/endermite.png", "", true, false),
    EVOKER("Evoker", "net.minecraft.entity.monster.EntityEvoker", true, 8, "textures/entity/illager/evoker.png", "", true, false),
    GHAST("Ghast", "net.minecraft.entity.monster.EntityGhast", true, 16, "textures/entity/ghast/ghast.png", "", true, false),
    GHASTATTACKING("Ghast", "net.minecraft.entity.monster.EntityGhast", false, 16, "textures/entity/ghast/ghast_shooting.png", "", true, false),
    GUARDIAN("Guardian", "net.minecraft.entity.monster.EntityGuardian", true, 6, "textures/entity/guardian.png", "", true, false),
    GUARDIANELDER("Elder_Guardian", "net.minecraft.entity.monster.EntityGuardian", true, 12, "textures/entity/guardian_elder.png", "", true, false),
    HORSE("Horse", "net.minecraft.entity.passive.EntityHorse", true, 8, "textures/entity/horse/horse_creamy.png", "", false, true),
    HUSK("Husk", "net.minecraft.entity.monster.EntityHusk", true, 8, "textures/entity/zombie/husk.png", "", true, false),
    ILLUSIONER("Illusioner", "net.minecraft.entity.monster.EntityIllusionIllager", true, 8, "textures/entity/illager/illusioner.png", "", true, false),
    IRONGOLEM("Iron_Golem", "net.minecraft.entity.passive.EntityIronGolem", true, 8, "textures/entity/iron_golem.png", "", false, true),
    LLAMA("Llama", "net.minecraft.entity.passive.EntityLlama", true, 8, "textures/entity/llama/brown.png", "", false, true),
    MAGMA("Magma_Cube", "net.minecraft.entity.monster.EntityMagmaCube", true, 8, "textures/entity/slime/magmacube.png", "", true, false),
    MOOSHROOM("Mooshroom", "net.minecraft.entity.passive.EntityMooshroom", true, 40, "textures/entity/cow/mooshroom.png", "", false, true),
    OCELOT("Ocelot", "net.minecraft.entity.passive.EntityOcelot", true, 5, "textures/entity/cat/ocelot.png", "", false, true),
    PARROT("Parrot", "net.minecraft.entity.passive.EntityParrot", true, 8, "textures/entity/parrot/parrot_red_blue.png", "", false, true),
    PHANTOM("Phantom", "net.minecraft.entity.monster.EntityPhantom", true, 10, "textures/entity/phantom.png", "", true, false),
    PIG("Pig", "net.minecraft.entity.passive.EntityPig", true, 8, "textures/entity/pig/pig.png", "", false, true),
    PIGZOMBIE("Zombie_Pigman", "net.minecraft.entity.monster.EntityPigZombie", true, 8, "textures/entity/zombie_pigman.png", "", true, true),
    PLAYER("Player", "net.minecraft.entity.player.EntityPlayerMP", false, 8, "textures/entity/steve.png", "", false, false),
    POLARBEAR("Polar_Bear", "net.minecraft.entity.monster.EntityPolarBear", true, 9, "textures/entity/bear/polarbear.png", "", true, true),
    RABBIT("Rabbit", "net.minecraft.entity.passive.EntityRabbit", true, 5, "textures/entity/rabbit/salt.png", "", false, true),
    PUFFERFISH("Pufferfish", "net.minecraft.entity.passive.EntityPufferFish", true, 3, "textures/entity/fish/pufferfish.png", "", false, true),
    PUFFERFISHHALF("Pufferfish_Half", "net.minecraft.entity.passive.EntityPufferFish", false, 5, "textures/entity/fish/pufferfish.png", "", false, true),
    PUFFERFISHFULL("Pufferfish_Full", "net.minecraft.entity.passive.EntityPufferFish", false, 8, "textures/entity/fish/pufferfish.png", "", false, true),
    SALMON("Salmon", "net.minecraft.entity.passive.EntitySalmon", true, 13, "textures/entity/fish/salmon.png", "", false, true),
    SHEEP("Sheep", "net.minecraft.entity.passive.EntitySheep", true, 6, "textures/entity/sheep/sheep.png", "", false, true),
    SHULKER("Shulker", "net.minecraft.entity.monster.EntityShulker", true, 6, "textures/entity/shulker/shulker_purple.png", "", true, false),
    SILVERFISH("Silverfish", "net.minecraft.entity.monster.EntitySilverfish", true, 6, "textures/entity/silverfish.png", "", true, false),
    SKELETON("Skeleton", "net.minecraft.entity.monster.EntitySkeleton", true, 8, "textures/entity/skeleton/skeleton.png", "", true, false),
    SKELETONWITHER(
            "Wither_Skeleton", "net.minecraft.entity.monster.EntityWitherSkeleton", true, 8, "textures/entity/skeleton/wither_skeleton.png", "", true, false
    ),
    SLIME("Slime", "net.minecraft.entity.monster.EntitySlime", true, 8, "textures/entity/slime/slime.png", "", true, false),
    SNOWGOLEM("Snow_Golem", "net.minecraft.entity.monster.EntitySnowman", true, 8, "textures/entity/snow_golem.png", "", false, true),
    SPIDER("Spider", "net.minecraft.entity.monster.EntitySpider", true, 8, "textures/entity/spider/spider.png", "", true, false),
    SQUID("Squid", "net.minecraft.entity.passive.EntitySquid", true, 6, "textures/entity/squid.png", "", false, true),
    STRAY(
            "Stray",
            "net.minecraft.entity.monster.EntityStray",
            true,
            8,
            "textures/entity/skeleton/stray.png",
            "textures/entity/skeleton/stray_overlay.png",
            true,
            false
    ),
    TROPICALFISHA(
            "Tropical_Fish",
            "net.minecraft.entity.passive.EntityTropicalFish",
            true,
            5,
            "textures/entity/fish/tropical_a.png",
            "textures/entity/fish/tropical_a_pattern_1.png",
            false,
            true
    ),
    TROPICALFISHB(
            "Tropical_Fish",
            "net.minecraft.entity.passive.EntityTropicalFish",
            false,
            6,
            "textures/entity/fish/tropical_b.png",
            "textures/entity/fish/tropical_b_pattern_4.png",
            false,
            true
    ),
    TURTLE("Turtle", "net.minecraft.entity.passive.EntityTurtle", true, 6, "textures/entity/turtle/big_sea_turtle.png", "", false, true),
    VEX("Vex", "net.minecraft.entity.monster.EntityVex", true, 8, "textures/entity/illager/vex.png", "", true, false),
    VEXCHARGING("Vex", "net.minecraft.entity.monster.EntityVex", false, 8, "textures/entity/illager/vex_charging.png", "", true, false),
    VILLAGER("Villager", "net.minecraft.entity.passive.EntityVillager", true, 8, "textures/entity/villager/farmer.png", "", false, true),
    VINDICATOR("Vindicator", "net.minecraft.entity.monster.EntityVindicator", true, 8, "textures/entity/illager/vindicator.png", "", true, false),
    WITCH("Witch", "net.minecraft.entity.monster.EntityWitch", true, 10, "textures/entity/witch.png", "", true, false),
    WITHER("Wither", "net.minecraft.entity.boss.EntityWither", true, 24, "textures/entity/wither/wither.png", "", true, false),
    WITHERINVULNERABLE("Wither", "net.minecraft.entity.boss.EntityWither", false, 24, "textures/entity/wither/wither_invulnerable.png", "", true, false),
    WOLF("Wolf", "net.minecraft.entity.passive.EntityWolf", true, 6, "textures/entity/wolf/wolf.png", "", true, true),
    WOLFANGRY("Wolf", "net.minecraft.entity.passive.EntityWolf", false, 6, "textures/entity/wolf/wolf_angry.png", "", true, false),
    WOLFTAME("Wolf", "net.minecraft.entity.passive.EntityWolf", false, 6, "textures/entity/wolf/wolf_tame.png", "", false, true),
    ZOMBIE("Zombie", "net.minecraft.entity.monster.EntityZombie", true, 8, "textures/entity/zombie/zombie.png", "", true, false),
    ZOMBIEVILLAGER(
            "Zombie", "net.minecraft.entity.monster.EntityZombieVillager", false, 8, "textures/entity/zombie_villager/zombie_villager.png", "", true, false
    ),
    UNKNOWN("Unknown", "", false, 8, "/mob/uknown.png", "", true, true),
    CUSTOM("Custom", "", false, 8, "/mob/unknown.png", "", true, true),
    AUTO("Auto", "", false, 8, "/mob/unknown.png", "", true, true);

    public final String id;
    public final String classPath;
    public final boolean isTopLevelUnit;
    public final int expectedWidth;
    public final ResourceLocation resourceLocation;
    public final boolean isHostile;
    public final boolean isNeutral;
    public ResourceLocation secondaryResourceLocation;
    public boolean enabled;

    EnumMobs(String name, String classPath, boolean topLevelUnit, int expectedWidth, String path, String secondaryPath, boolean isHostile, boolean isNeutral) {
        this.id = name;
        this.classPath = classPath.toLowerCase();
        this.isTopLevelUnit = topLevelUnit;
        this.expectedWidth = expectedWidth;
        this.resourceLocation = new ResourceLocation(path.toLowerCase());
        this.secondaryResourceLocation = secondaryPath.equals("") ? null : new ResourceLocation(secondaryPath.toLowerCase());
        this.isHostile = isHostile;
        this.isNeutral = isNeutral;
        this.enabled = true;
    }

    public static EnumMobs getMobByName(String par0) {
        for (EnumMobs enumMobs : values()) {
            if (enumMobs.id.equals(par0)) {
                return enumMobs;
            }
        }

        return null;
    }

    public int returnEnumOrdinal() {
        return this.ordinal();
    }
}
