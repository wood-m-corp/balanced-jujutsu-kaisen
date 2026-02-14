package radon.jujutsu_kaisen.config;

import net.minecraftforge.common.ForgeConfigSpec;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerConfig {
    public final ForgeConfigSpec.DoubleValue cursedEnergyAmount;
    public final ForgeConfigSpec.DoubleValue cursedEnergyRegenerationAmount;
    public final ForgeConfigSpec.DoubleValue cursedEnergyCostAmount;
    public final ForgeConfigSpec.DoubleValue maximumExperienceAmount;
    public final ForgeConfigSpec.DoubleValue Grade4Exp;
    public final ForgeConfigSpec.DoubleValue Grade3Exp;
    public final ForgeConfigSpec.DoubleValue SemiGrade2Exp;
    public final ForgeConfigSpec.DoubleValue Grade2Exp;
    public final ForgeConfigSpec.DoubleValue SemiGrade1Exp;
    public final ForgeConfigSpec.DoubleValue Grade1Exp;
    public final ForgeConfigSpec.DoubleValue SpecialGrade1Exp;
    public final ForgeConfigSpec.DoubleValue SpecialGradeExp;
    public final ForgeConfigSpec.DoubleValue cursedObjectEnergyForGrade;
    public final ForgeConfigSpec.IntValue bindingVowCooldown;
    public final ForgeConfigSpec.IntValue livesconfig;
    public final ForgeConfigSpec.IntValue reverseCursedTechniqueChance;
    public final ForgeConfigSpec.IntValue totemRCTChanceMult;
    public final ForgeConfigSpec.DoubleValue requiredExperienceForExperienced;
    //public final ForgeConfigSpec.DoubleValue domainExpReq;
    public final ForgeConfigSpec.DoubleValue wcsExpMahoReq;
    public final ForgeConfigSpec.DoubleValue wcsExpOtherReq;
    public final ForgeConfigSpec.IntValue sorcererFleshRarity;
    public final ForgeConfigSpec.IntValue curseFleshRarity;
    public final ForgeConfigSpec.IntValue sorcererVillageSpawnRate;
    public final ForgeConfigSpec.IntValue curseVillageSpawnRate;
    public final ForgeConfigSpec.IntValue displayCaseSpawnRate;
    public final ForgeConfigSpec.IntValue displayCaseSpawnRange;
    public final ForgeConfigSpec.IntValue disasterCurseSpawnRate;
    public final ForgeConfigSpec.IntValue minimumSpawnDangerDistance;
    public final ForgeConfigSpec.DoubleValue pointMultiplier;
    public final ForgeConfigSpec.DoubleValue experienceMultiplier;
    public final ForgeConfigSpec.DoubleValue minimumBodyStealEXP;
    public final ForgeConfigSpec.DoubleValue deathPenalty;
    public final ForgeConfigSpec.DoubleValue pointPenalty;
    public final ForgeConfigSpec.DoubleValue pvpGain;
    public final ForgeConfigSpec.IntValue minPoints;
    public final ForgeConfigSpec.IntValue maxPoints;
    public final ForgeConfigSpec.DoubleValue minEXP;
    public final ForgeConfigSpec.DoubleValue maxEXP;
    public final ForgeConfigSpec.IntValue blackFlashChanceRNG;
    public final ForgeConfigSpec.DoubleValue blackFlashPower;
    public final ForgeConfigSpec.DoubleValue blackFlashDmgCap;
    public final ForgeConfigSpec.BooleanValue newShadowStyleForAll;
    public final ForgeConfigSpec.BooleanValue incarnatedSimpleDomain;
    public final ForgeConfigSpec.BooleanValue realisticShikigami;
    public final ForgeConfigSpec.BooleanValue realisticCurses;
    public final ForgeConfigSpec.BooleanValue sorcererSaturation;
    public final ForgeConfigSpec.BooleanValue curseSaturation;
    public final ForgeConfigSpec.BooleanValue foodCERegen;
    public final ForgeConfigSpec.BooleanValue playerMimicry;
    public final ForgeConfigSpec.BooleanValue playerBodySteal;
    public final ForgeConfigSpec.BooleanValue bodyStealEXPReset;
    public final ForgeConfigSpec.BooleanValue mimicryBodyStealCompat;
    public final ForgeConfigSpec.BooleanValue bodyStealTraits;
    public final ForgeConfigSpec.BooleanValue bodyStealReroll;
    public final ForgeConfigSpec.BooleanValue MBAReroll;
    public final ForgeConfigSpec.BooleanValue MBAEXPReset;
    public final ForgeConfigSpec.BooleanValue MBADeath;
    public final ForgeConfigSpec.BooleanValue wcsCutAnything;
    public final ForgeConfigSpec.BooleanValue hrRequiredForISOH;
    public final ForgeConfigSpec.BooleanValue playerRequiredForRCT;
    public final ForgeConfigSpec.BooleanValue playerRequiredForGradeUp;

    public final ForgeConfigSpec.DoubleValue sorcererHealingAmount;
    public final ForgeConfigSpec.DoubleValue curseHealingAmount;
    public final ForgeConfigSpec.DoubleValue curseDamageMult;
    public final ForgeConfigSpec.DoubleValue curseDefenseMult;
    public final ForgeConfigSpec.DoubleValue sorcererDamageMult;
    public final ForgeConfigSpec.DoubleValue sorcererDefenseMult;
    public final ForgeConfigSpec.DoubleValue jujutsuDefenseMult;
    public final ForgeConfigSpec.DoubleValue hrDefenseMult;
    public final ForgeConfigSpec.DoubleValue playerDamageMult;
    public final ForgeConfigSpec.DoubleValue npcvsnpcDamageMult;
    public final ForgeConfigSpec.DoubleValue playerHPMult;
    public final ForgeConfigSpec.DoubleValue npcHPMult;
    public final ForgeConfigSpec.DoubleValue playerCEArmor;
    public final ForgeConfigSpec.DoubleValue playerCEArmorMax;
    public final ForgeConfigSpec.DoubleValue playerCEArmorMin;
    public final ForgeConfigSpec.IntValue playerHPMin;
    public final ForgeConfigSpec.DoubleValue playerMaxSpeed;
    public final ForgeConfigSpec.DoubleValue HRMaxSpeed;
    public final ForgeConfigSpec.IntValue npcHPMin;
    public final ForgeConfigSpec.DoubleValue phrHPMult;
    public final ForgeConfigSpec.IntValue phrHPMin;
    public final ForgeConfigSpec.IntValue cehrHP;
    public final ForgeConfigSpec.DoubleValue cehrCEMult;
    public final ForgeConfigSpec.IntValue cehrOutputMax;
    public final ForgeConfigSpec.DoubleValue playerM1Mult;
    public final ForgeConfigSpec.DoubleValue limitlessNoSixEyesMult;
    public final ForgeConfigSpec.DoubleValue sixEyesMult;
    public final ForgeConfigSpec.DoubleValue perfectBodyMult;




    public final ForgeConfigSpec.BooleanValue uniqueTechniques;
    public final ForgeConfigSpec.BooleanValue uniqueTraits;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> uniqueTraitList;
    public final ForgeConfigSpec.BooleanValue destruction;
    public final ForgeConfigSpec.BooleanValue turboMode;
    public final ForgeConfigSpec.BooleanValue entitySlicing;
    public final ForgeConfigSpec.BooleanValue chantRequiredForWCS;

    public final ForgeConfigSpec.IntValue minimumVeilSize;
    public final ForgeConfigSpec.IntValue maximumVeilSize;
    public final ForgeConfigSpec.DoubleValue minimumDomainSize;
    public final ForgeConfigSpec.DoubleValue maximumDomainSize;

    public final ForgeConfigSpec.IntValue maximumChantCount;
    public final ForgeConfigSpec.IntValue minimumChantLength;
    public final ForgeConfigSpec.IntValue maximumChantLength;
    public final ForgeConfigSpec.DoubleValue chantSimilarityThreshold;

    public final ForgeConfigSpec.IntValue simpleDomainCost;
    public final ForgeConfigSpec.IntValue simpleDomainEnlargementCost;
    public final ForgeConfigSpec.IntValue maximumUzumakiCost;
    public final ForgeConfigSpec.IntValue miniUzumakiCost;
    public final ForgeConfigSpec.IntValue maximumMeteorCost;
    public final ForgeConfigSpec.IntValue ceBombCost;
    public final ForgeConfigSpec.IntValue ceBlastCost;
    public final ForgeConfigSpec.IntValue quickDrawCost;
    public final ForgeConfigSpec.IntValue hollowWickerBasketCost;
    public final ForgeConfigSpec.IntValue fallingBlossomEmotionCost;
    public final ForgeConfigSpec.IntValue domainExpansionCost;
    public final ForgeConfigSpec.IntValue domainAmplificationCost;
    public final ForgeConfigSpec.IntValue zeroPointTwoSecondDomainExpansionCost;
    public final ForgeConfigSpec.IntValue rct2Cost;
    public final ForgeConfigSpec.IntValue rct3Cost;
    public final ForgeConfigSpec.IntValue outputRCTCost;
    public final ForgeConfigSpec.IntValue maximumCopiedTechniques;
    public final ForgeConfigSpec.IntValue maximumStolenTechniques;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> unlockableSorcererTechniques;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> unlockableCursedSpiritTechniques;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sorcererTraitList;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> curseTraitList;
    public final ForgeConfigSpec.ConfigValue<List<String>> incompatibleTraits;

    public final ForgeConfigSpec.IntValue natureTraitModifier;
    public final ForgeConfigSpec.IntValue traitScalingModifier;
    public final ForgeConfigSpec.IntValue natureTraitCost;
    public final ForgeConfigSpec.IntValue traitRolls;
    public final ForgeConfigSpec.IntValue minTraits;
    public final ForgeConfigSpec.IntValue maxTraits;
    public final ForgeConfigSpec.IntValue noTraitWeight;
    public final ForgeConfigSpec.IntValue cursedEnergyNatureRarity;
    public final ForgeConfigSpec.IntValue curseRarity;
    public final ForgeConfigSpec.IntValue sixEyesWeight;
    public final ForgeConfigSpec.IntValue heavenlyRestrictionRarity;
    public final ForgeConfigSpec.IntValue vesselWeight;
    public final ForgeConfigSpec.IntValue rctOutputWeight;
    public final ForgeConfigSpec.IntValue perfectBodyWeight;
    public final ForgeConfigSpec.IntValue incarnatedWeight;
    public final ForgeConfigSpec.IntValue prodigyWeight;
    public final ForgeConfigSpec.IntValue cursedWombWeight;
    public final ForgeConfigSpec.IntValue deathPaintingWeight;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Progression").push("progression");
        this.cursedEnergyAmount = builder.comment("Cursed energy amount (scales with experience)")
                .defineInRange("cursedEnergyAmount", 500.0F, 0.0F, 1000000.0F);
        this.cursedEnergyRegenerationAmount = builder.comment("Cursed energy regeneration amount (depends on food level)")
                .defineInRange("cursedEnergyRegenerationAmount", 0.6F, 0.0F, 100000.0F);
        this.cursedEnergyCostAmount = builder.comment("Drain Multiplier of Cursed Energy (recommended to maintain the ratio with regen)")
                .defineInRange("cursedEnergyCostAmount", 1.0F, 0.0F, 100000.0F);
        this.maximumExperienceAmount = builder.comment("The maximum amount of experience one can obtain")
                .defineInRange("maximumExperienceAmount", 20000.0F, 1.0F, 1000000.0F);
        this.Grade4Exp = builder.comment("The experience required for Grade 4 (affects npcs)")
                .defineInRange("Grade4Exp", 0.0F, 0.0F, 1000000.0F);
        this.Grade3Exp = builder.comment("The experience required for Grade 3 (affects npcs)")
                .defineInRange("Grade3Exp", 500.0F, 0.0F, 1000000.0F);
        this.SemiGrade2Exp = builder.comment("The experience required for Semi Grade 2 (affects npcs)")
                .defineInRange("SemiGrade2Exp", 1000.0F, 0.0F, 1000000.0F);
        this.Grade2Exp = builder.comment("The experience required for Grade 2 (affects npcs)")
                .defineInRange("Grade2Exp", 1500.0F, 0.0F, 1000000.0F);
        this.SemiGrade1Exp = builder.comment("The experience required for Semi Grade 1 (affects npcs)")
                .defineInRange("SemiGrade1Exp", 2000.0F, 0.0F, 1000000.0F);
        this.Grade1Exp = builder.comment("The experience required for Grade 1 (affects npcs)")
                .defineInRange("Grade1Exp", 2500.0F, 0.0F, 1000000.0F);
        this.SpecialGrade1Exp = builder.comment("The experience required for Special Grade 1 (affects npcs)")
                .defineInRange("SpecialGrade1Exp", 3000.0F, 0.0F, 1000000.0F);
        this.SpecialGradeExp = builder.comment("The experience required for Special Grade (affects npcs)")
                .defineInRange("SpecialGradeExp", 4000.0F, 0.0F, 1000000.0F);
        this.cursedObjectEnergyForGrade = builder.comment("The amount of energy consuming cursed objects gives to curses (multiplied by the grade of the object)")
                .defineInRange("cursedObjectEnergyForGrade", 100.0F, 1.0F, 1000.0F);
        this.bindingVowCooldown = builder.comment("Cooldown after removing a Binding Vow in seconds (default is 30 Minutes")
                .defineInRange("bindingVowCooldown", 0, 0, 1800);
        this.livesconfig = builder.comment("Max deaths before a player is rerolled (default 0 = disabled)")
                .defineInRange("livesconfig", 0, 0, 1000);
        this.reverseCursedTechniqueChance = builder.comment("The chance of unlocking reverse cursed technique when dying (smaller number equals bigger chance)")
                .defineInRange("reverseCursedTechniqueChance", 20, 1, 1000);
        this.totemRCTChanceMult = builder.comment("The amount the chance is divided by when holding a totem (raises chances of obtaining rct the higher it is) ")
                .defineInRange("totemRCTChanceMult", 4, 1, 1000);
        this.requiredExperienceForExperienced = builder.comment("The amount of experience required for a player to be classified as experienced (for now means they can use domain amplification during a domain expansion/wheel w domain amp)")
                .defineInRange("requiredExperienceForExperienced", 5000.0F, 1.0F, 1000000.0F);
        // this.domainExpReq = builder.comment("Experience required to learn a Domain Expansion")
        //         .defineInRange("domainExpReq", 3000.0F, 0.0F, 1000000.0F);
        this.wcsExpMahoReq = builder.comment("Experience required to learn WCS from Mahoraga")
                .defineInRange("wcsExpMahoReq", 10000.0F, 0.0F, 1000000.0F);
        this.wcsExpOtherReq = builder.comment("Experience required to learn WCS from other sources")
                .defineInRange("wcsExpOtherReq", 10000.0F, 0.0F, 1000000.0F);
        this.sorcererFleshRarity = builder.comment("Rarity of sorcerers dropping flesh (bigger value means more rare, 0 to disable)")
                .defineInRange("sorcererFleshRarity", 20, 0, 100000);
        this.curseFleshRarity = builder.comment("Rarity of curses dropping flesh (bigger value means more rare, 0 to disable)")
                .defineInRange("curseFleshRarity", 20, 0, 100000);
        this.curseVillageSpawnRate = builder.comment("Rarity of curses spawning in villages (bigger value means more rare, 0 to disable)")
                .defineInRange("curseVillageSpawnRate", 8, 0, 100000);
        this.sorcererVillageSpawnRate = builder.comment("Rarity of sorcerers spawning in villages (bigger value means more rare, 0 to disable)")
                .defineInRange("sorcererVillageSpawnRate", 7, 0, 100000);
        this.displayCaseSpawnRate = builder.comment("Rarity of curses spawning from display cases (bigger value means more rare, 0 to disable)")
                .defineInRange("displayCaseRarity", 11, 0, 100000);
        this.displayCaseSpawnRange = builder.comment("Range in blocks curses can spawn from display cases")
                .defineInRange("displayCaseSpawnRange", 64, 0, 100000);
        this.disasterCurseSpawnRate = builder.comment("Rarity of disaster curses (bigger value means more rare, 0 to disable)")
                .defineInRange("disasterCurseSpawnRate", 12, 0, 100000);
        this.minimumSpawnDangerDistance = builder.comment("The minimum distance from spawn of dangerous things such as disaster curses")
                .defineInRange("minimumSpawnDangerDistance", 1000, 0, 100000);
        this.experienceMultiplier = builder.comment("Scale of experience you gain")
                .defineInRange("experienceMultiplier", 1.0F, 0.0F, 100.0F);
        this.minimumBodyStealEXP = builder.comment("Minimum EXP before a body can be stolen")
                .defineInRange("minimumBodyStealEXP", 0.0F, 0.0F, 100000.0F);
        this.pointMultiplier = builder.comment("Scale of ability points you gain")
                .defineInRange("pointMultiplier", 0.13F, 0.0F, 100.0F);
        this.pointPenalty = builder.comment("Scale of points lost on death")
                .defineInRange("pointPenalty", 0.0F, 0.0F, 100.0F);
        this.deathPenalty = builder.comment("Percentage of experience lost on death")
                .defineInRange("deathPenalty", 0.0F, 0.0F, 1.0F);
        this.minPoints = builder.comment("Minimum points gained from battles")
                .defineInRange("minPoints", 0, 0, 9999999);
        this.maxPoints = builder.comment("Maximum points gained from battles")
                .defineInRange("maxPoints", 0, 0, 9999999);
        this.minEXP = builder.comment("Minimum experience gained from battles")
                .defineInRange("minExp", 1.0F, 0.0F, 9999999.0F);
        this.maxEXP = builder.comment("Maximum experience gained from battles (0 to disable)")
                .defineInRange("maxExp", 0.0F, 0.0F, 9999999.0F);
        this.pvpGain = builder.comment("Percentage of experience gained from player kills")
                .defineInRange("pvpGain", 1.0F, 0.0F, 999.0F);
        this.blackFlashChanceRNG = builder.comment("The chance of black flash (smaller number equals bigger chance)")
                .defineInRange("blackFlashChanceRNG", 200, 0, 1000);
        this.blackFlashPower = builder.comment("The multiplier a black flash ('power of' multiplier to bfs, canon by default)")
                .defineInRange("blackFlashPower", 2.5F, 1.0F, 1000.0F);
        this.blackFlashDmgCap = builder.comment("The maximum damage of a black flash attack (entire dmg not just the added dmg)")
                .defineInRange("blackFlashDmgCap", 40.0F, 0.0F, 999999.0F);
        this.newShadowStyleForAll = builder.comment("When enabled anyone may learn advanced Simple Domain Techs (techniqueless is useless with this)")
                .define("newShadowStyleForAll", false);
        this.incarnatedSimpleDomain = builder.comment("When enabled Incarnated Sorcerers may use Simple Domain")
                .define("incarnatedSimpleDomain", true);
        this.realisticShikigami = builder.comment("When enabled Ten Shadows shikigami will die permanently")
                .define("realisticShikigami", false);
        this.realisticCurses = builder.comment("When enabled curses only take damage from jujutsu attacks")
                .define("realisticCurses", true);
        this.sorcererSaturation = builder.comment("When enabled Sorcerers will always have their hunger filled.")
                .define("sorcererSaturation", true);
        this.curseSaturation = builder.comment("When enabled Curses will always have their hunger filled.")
                .define("curseSaturation", true);
        this.foodCERegen = builder.comment("When enabled Cursed Energy regeneration speed will scale off hunger.")
                .define("foodCERegen", true);
        this.playerBodySteal = builder.comment("When enabled Body Steal only works on players")
                .define("playerBodySteal", false);
        this.playerMimicry = builder.comment("When enabled Mimicry only works on players")
                .define("playerMimicry", false);
        this.mimicryBodyStealCompat = builder.comment("When enabled Mimicry and Body Steal may steal from each other.")
                .define("mimicryBodyStealCompat", true);
        this.bodyStealTraits = builder.comment("Whether Body Steal should steal traits, besides RCT Output and Heavenly Restriction.")
                .define("bodyStealTraits", true);
        this.bodyStealEXPReset = builder.comment("Whether Body Steal should reset the EXP of the stolen player")
                .define("bodyStealEXPReset", true);
        this.bodyStealReroll = builder.comment("Whether Body Steal should reroll the player.")
                .define("bodyStealReroll", false);
        this.MBADeath = builder.comment("Whether Mythical Beast Amber should kill the user after use")
                .define("MBADeath", true);
        this.MBAEXPReset = builder.comment("Whether Mythical Beast Amber should reset the EXP of the user after use")
                .define("MBAEXPReset", true);
        this.MBAReroll = builder.comment("Whether Mythical Beast Amber should reroll the player.")
                .define("MBAReroll", false);
        this.wcsCutAnything = builder.comment("Whether World Cutting Slash truly cuts the world (destroys indestructible blocks).")
                .define("wcsCutAnything", true);
        this.hrRequiredForISOH = builder.comment("Whether Physical Heavenly Restriction is required to use the Inverted Spear of Heaven")
                .define("hrRequiredForISOH", false);
        this.playerRequiredForRCT = builder.comment("Whether Players must kill you in order for you to unlock RCT")
                .define("playerRequiredForRCT", false);
        this.playerRequiredForGradeUp = builder.comment("Whether Players must kill other players in order to Rank Up ")
                .define("playerRequiredForGradeUp", false);
        builder.pop();
    

        builder.comment("Miscellaneous").push("misc");
        this.sorcererHealingAmount = builder.comment("The maximum amount of health sorcerers can heal per tick (scales with experience)")
                .defineInRange("sorcererHealingAmount", 0.1F, 0.0F, 2.5F);
        this.curseHealingAmount = builder.comment("The maximum amount of health curses can heal per tick (scales with experience)")
                .defineInRange("curseHealingAmount", 0.15F, 0.0F, 2.5F);
        this.curseDamageMult = builder.comment("The multiplier on the damage NPC curses deal to you")
                .defineInRange("curseDamageMult", 0.7F, 0.0F, 9999.0F);
        this.curseDefenseMult = builder.comment("The multiplier on damage NPC curses take from you")
                .defineInRange("curseDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.sorcererDamageMult = builder.comment("The multiplier on the damage NPC sorcerers deal to you")
                .defineInRange("sorcererDamageMult", 0.7F, 0.0F, 9999.0F);
        this.sorcererDefenseMult = builder.comment("The multiplier on damage NPC sorcerers take from you")
                .defineInRange("sorcererDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.jujutsuDefenseMult = builder.comment("The multiplier to standard players' defense")
                .defineInRange("jujutsuDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.hrDefenseMult = builder.comment("The multiplier to Physical Heavenly Restriction players's defense (already higher outside of config)")
                .defineInRange("hrDefenseMult", 1.0F, 0.0F, 9999.0F);
        this.playerDamageMult = builder.comment("The multiplier to all player attacks (includes summons of all kinds)")
                .defineInRange("playerDamageMult", 1.0F, 0.0F, 9999.0F);
        this.npcvsnpcDamageMult = builder.comment("The multiplier to npc vs npc damage")
                .defineInRange("npcvsnpcDamageMult", 0.85F, 0.0F, 9999.0F);
        this.playerHPMult = builder.comment("The multiplier to player HP (scales by bars, so will move by 20 hp increments)")
                .defineInRange("playerHPMult", 15.0F, 0.0F, 9999.0F);
        this.npcHPMult = builder.comment("The multiplier to npc HP (scales by bars, so will move by 20 hp increments)")
                .defineInRange("npcHPMult", 15.0F, 0.0F, 9999.0F);
        this.playerCEArmor = builder.comment("The multiplier to player armor with ce flow/hr (does not pass cap)")
                .defineInRange("playerCEArmorMult", 8.0F, 0.0F, 9999.0F);
        this.playerCEArmorMin = builder.comment("The minimum boost to player armor with ce flow/hr (does not pass cap)")
                .defineInRange("playerCEArmorMin", 12.0F, 0.0F, 9999.0F);
        this.playerCEArmorMax = builder.comment("The maximum boost to player armor with ce flow/hr (This is the cap, does not stack w reg armor)")
                .defineInRange("playerCEArmorMax", 20.0F, 0.0F, 9999.0F);
        this.playerMaxSpeed = builder.comment("The maximum boost to player movement speed, scales to 0.32 at 20k exp by default (DOES NOT CAP PROJECTION OR SCALE SPEED HIGHER).")
                .defineInRange("playerMaxSpeed", 0.32F, 0.0F, 9999.0F);
        this.HRMaxSpeed = builder.comment("The maximum boost to Physical Heavenly Restriction movement speed, scales to 0.8 at 20k by default (DOES NOT SCALE SPEED HIGHER)")
                .defineInRange("HRMaxSpeed", 0.8F, 0.0F, 9999.0F);
        this.playerHPMin = builder.comment("The minimum health of a player.")
                .defineInRange("playerHPMin", 40, 1, 9999);
        this.phrHPMult = builder.comment("The multiplier to a Physical Heavenly Restriction player's HP (scales by bars, so will move by 20 hp increments)")
                .defineInRange("phrHPMult", 15.0F, 0.0F, 9999.0F);
        this.phrHPMin = builder.comment("The minimum health of a Physical Heavenly Restriction player.")
                .defineInRange("phrHPMin", 40, 1, 9999);
        this.cehrHP = builder.comment("The health value of a CE Heavenly Restriction player.")
                .defineInRange("cehrHPMin", 10, 1, 9999);
        this.cehrCEMult = builder.comment("The CE regeneration multiplier applied to a CE Heavenly Restriction player.")
                .defineInRange("cehrCEMult", 2.0F, 1.0F, 9999.0F);
        this.cehrOutputMax = builder.comment("The maximum output of CE for a CE Heavenly Restriction player.")
                .defineInRange("cehrOutputMax", 500, 100, 9999);
        this.npcHPMin = builder.comment("The minimum health of the mod's NPCs.")
                .defineInRange("npcHPMin", 40, 1, 9999);
        this.playerM1Mult = builder.comment("The multiplier to player M1 Hit Damage")
                .defineInRange("playerM1Mult", 2.0F, 0.0F, 9999.0F);
        this.limitlessNoSixEyesMult = builder.comment("The multiplier to Limitless's costs without Six Eyes")
                .defineInRange("limitlessNoSixEyesMult", 1.0F, 0.0F, 9999.0F);
        this.sixEyesMult = builder.comment("The multiplier of drain decreases given to Six Eyes.")
                .defineInRange("sixEyesMult", 0.5F, 0.0F, 9999.0F);
        this.perfectBodyMult = builder.comment("The multiplier of Perfect Body's damage.")
                .defineInRange("perfectBodyMult", 1.5F, 0.0F, 9999.0F);

        this.uniqueTechniques = builder.comment("When enabled on servers every player will have a unique technique if any are available")
                .define("uniqueTechniques", true);
        this.uniqueTraits = builder.comment("When enabled on servers there can be only one Six Eyes and Perfect Body")
                .define("uniqueTraits", true);
        this.uniqueTraitList = builder.comment("Traits that will be Unique under the config")
                .defineList("uniqueTraitList", () -> List.of(
                                Trait.SIX_EYES.name(),
                                Trait.PERFECT_BODY.name()
                        ), ignored -> true);
        this.destruction = builder.comment("When enabled abilities break blocks")
                .define("destruction", true);
        this.turboMode = builder.comment("When enabled abilities have no cooldowns for players")
                .define("turboMode", false);
        this.entitySlicing = builder.comment("When enabled entities are sliced by Dismantle (may cause shader/mod incompat)")
                .define("entitySlicing", false);
        this.chantRequiredForWCS = builder.comment("When enabled WCS must be chanted to 150%")
                .define("chantRequiredForWCS", true);
        builder.pop();

        builder.comment("Veils").push("veils");
        this.minimumVeilSize = builder.comment("Minimum size for a veil")
                .defineInRange("minimumVeilSize", 4, 4, 64);
        this.maximumVeilSize = builder.comment("Maximum size for a veil")
                .defineInRange("maximumVeilSize", 64, 32, 256);
        builder.pop();

        builder.comment("Domains").push("domains");
        this.minimumDomainSize = builder.comment("Minimum size for a domain")
                .defineInRange("minimumDomainSize", 0.5F, 0.2F, 1.0F);
        this.maximumDomainSize = builder.comment("Maximum size for a domain")
                .defineInRange("maximumDomainSize", 1.5F, 1.0F, 10.0F);
        builder.pop();

        builder.comment("Chants").push("chants");
        this.maximumChantCount = builder.comment("Maximum count for chants")
                .defineInRange("maximumChantCount", 5, 1, 16);
        this.minimumChantLength = builder.comment("Maximum length for a chant")
                .defineInRange("minimumChantLength", 2, 1, 256);
        this.maximumChantLength = builder.comment("Maximum length for a chant")
                .defineInRange("maximumChantLength", 24, 1, 256);
        this.chantSimilarityThreshold = builder.comment("Minimum difference between chants for them to be valid")
                .defineInRange("chantSimilarityThreshold", 0.25F, 0.0F, 1.0F);
        builder.pop();

        builder.comment("Abilities").push("abilities");
        this.simpleDomainCost = builder.comment("The amount of points simple domain costs to unlock")
                .defineInRange("simpleDomainCost", 25, 1, 10000);
        this.simpleDomainEnlargementCost = builder.comment("The amount of points simple domain enlargement costs to unlock")
                .defineInRange("simpleDomainEnlargementCost", 50, 1, 10000);
        this.quickDrawCost = builder.comment("The amount of points quick draw costs to unlock")
                .defineInRange("simpleDomainCost", 25, 1, 10000);
        this.fallingBlossomEmotionCost = builder.comment("The amount of points falling blossom emotion costs to unlock")
                .defineInRange("fallingBlossomEmotionCost", 25, 1, 10000);
        this.hollowWickerBasketCost = builder.comment("The amount of points hollow wicker basket costs to unlock")
                .defineInRange("hollowWickerBasketCost", 25, 1, 10000);
        this.domainExpansionCost = builder.comment("The amount of points domain expansion costs to unlock")
                .defineInRange("domainExpansionCost", 200, 1, 10000);
        this.domainAmplificationCost = builder.comment("The amount of points domain amplification costs to unlock")
                .defineInRange("domainAmplificationCost", 50, 1, 10000);
        this.zeroPointTwoSecondDomainExpansionCost = builder.comment("The amount of points 0.2s domain expasnion costs to unlock")
                .defineInRange("zeroPointTwoSecondDomainExpansionCost", 75, 1, 10000);
        this.miniUzumakiCost = builder.comment("The amount of points Mini Uzumaki costs to unlock")
                .defineInRange("miniUzumakiCost", 50, 1, 10000);
        this.maximumUzumakiCost = builder.comment("The amount of points Maximum: Uzumaki costs to unlock")
                .defineInRange("maximumUzumakiCost", 75, 1, 10000);
        this.maximumMeteorCost = builder.comment("The amount of points Maximum: Meteor costs to unlock")
                .defineInRange("maximumMeteorCost", 100, 1, 10000);
        this.ceBombCost = builder.comment("The amount of points Cursed Energy Bomb costs to unlock")
                .defineInRange("ceBombCost", 25, 1, 10000);
        this.ceBlastCost = builder.comment("The amount of points Cursed Energy Blast costs to unlock")
                .defineInRange("ceBlastCost", 25, 1, 10000);
        this.rct2Cost = builder.comment("The amount of points tier 2 RCT costs to unlock")
                .defineInRange("rct2Cost", 50, 1, 10000);
        this.rct3Cost = builder.comment("The amount of points tier 3 RCT costs to unlock")
                .defineInRange("rct3Cost", 100, 1, 10000);
        this.outputRCTCost = builder.comment("The amount of points output RCT costs to unlock")
                .defineInRange("outputRCTCost", 75, 1, 10000);
        this.maximumCopiedTechniques = builder.comment("The amount of techniques mimicry can copy")
                .defineInRange("maximumCopiedTechniques", 3, 1, 10000);
        this.maximumStolenTechniques = builder.comment("The amount of techniques that can be stolen")
                .defineInRange("maximumStolenTechniques", 2, 1, 10000);
        this.unlockableSorcererTechniques = builder.comment("Techniques that are unlockable for sorcerers by default")
                .defineList("unlockableSorcererTechniques", () -> List.of(
                                CursedTechnique.CURSE_MANIPULATION.name(),
                                CursedTechnique.LIMITLESS.name(),
                                CursedTechnique.SHRINE.name(),
                                CursedTechnique.CURSED_SPEECH.name(),
                                CursedTechnique.MIMICRY.name(),
                                CursedTechnique.DISASTER_FLAMES.name(),
                                CursedTechnique.DISASTER_TIDES.name(),
                                CursedTechnique.DISASTER_PLANTS.name(),
                                CursedTechnique.ANGEL.name(),
                                CursedTechnique.BRAIN_TRANSPLANT.name(),
                                CursedTechnique.TEN_SHADOWS.name(),
                                CursedTechnique.BOOGIE_WOOGIE.name(),
                                CursedTechnique.PROJECTION_SORCERY.name(),
                                CursedTechnique.RATIO.name(),
                                CursedTechnique.MYTHICAL_BEAST_AMBER.name(),
                                CursedTechnique.TECHNIQUELESS.name()
                        ),
                        ignored -> true
                );

        this.unlockableCursedSpiritTechniques = builder.comment("Techniques that are unlockable by Curses by default")
                .defineList("unlockableTechniques", () -> List.of(
                                CursedTechnique.CURSE_MANIPULATION.name(),
                                CursedTechnique.LIMITLESS.name(),
                                CursedTechnique.SHRINE.name(),
                                CursedTechnique.CURSED_SPEECH.name(),
                                CursedTechnique.MIMICRY.name(),
                                CursedTechnique.DISASTER_FLAMES.name(),
                                CursedTechnique.DISASTER_TIDES.name(),
                                CursedTechnique.DISASTER_PLANTS.name(),
                                CursedTechnique.ANGEL.name(),
                                CursedTechnique.IDLE_TRANSFIGURATION.name(),
                                CursedTechnique.TEN_SHADOWS.name(),
                                CursedTechnique.BOOGIE_WOOGIE.name(),
                                CursedTechnique.PROJECTION_SORCERY.name(),
                                CursedTechnique.RATIO.name(),
                                CursedTechnique.MYTHICAL_BEAST_AMBER.name(),
                                CursedTechnique.TECHNIQUELESS.name()
                        ),
                        ignored -> true
                );

           this.sorcererTraitList = builder.comment("Traits that can be rolled by Sorcerers (HR and Simurian may not be added here or else it might crash)")
                .defineList("sorcererTraitList", () -> List.of(
                                Trait.PRODIGY.name(),
                                Trait.PERFECT_BODY.name(),
                                Trait.RCT_OUTPUT.name(),
                                Trait.INCARNATED.name(),
                                Trait.VESSEL.name(),
                                Trait.SIX_EYES.name()
                        ), ignored -> true);
           this.curseTraitList = builder.comment("Traits that can be rolled by Curses (HR and Simurian may not be added here or else it might crash)")
                .defineList("curseTraitList", () -> List.of(
                                Trait.PRODIGY.name(),
                                Trait.PERFECT_BODY.name(),
                                Trait.CURSED_WOMB.name(),
                                Trait.DEATH_PAINTING.name()
                        ), ignored -> true);
           this.incompatibleTraits = builder.comment("Incompatible traits, formatted as TRAIT1,TRAIT2. To add more, just add more comma separated traits to the list")
                .define("incompatibleTraits", List.of("PERFECT_BODY,SIX_EYES", "CURSED_WOMB,DEATH_PAINTING"));
        builder.pop();

        builder.comment("Rarity").push("rarity");
        this.traitRolls = builder.comment("How many rolls are done to give players traits? (each minimum guaranteed trait takes a trait roll)")
                .defineInRange("traitRolls", 3, 0, 1000000);
        this.minTraits = builder.comment("The minimum amount of traits a player will start with")
                .defineInRange("minTraits", 0 ,0, 1000000);
        this.maxTraits = builder.comment("The maximum amount of traits a player can start with")
                .defineInRange("maxTraits", 6 ,0, 1000000);
        this.natureTraitCost = builder.comment("Cursed Energy Nature trait count cost (affected by the minimum/max amount of traits)")
                .defineInRange("natureTraitCost", 1 ,0, 1000000);
        this.natureTraitModifier = builder.comment("The division to the chance of rolling a trait with a nature")
                .defineInRange("natureTraitModifier", 2 ,0, 1000000);
        this.traitScalingModifier = builder.comment("The division to the chance of rolling an additional trait per trait rolled")
                .defineInRange("traitScalingModifier", 2 ,0, 1000000);
        this.noTraitWeight = builder.comment("Weight of receiving no trait")
                .defineInRange("noTraitWeight", 800, 0, 1000000);
        this.cursedEnergyNatureRarity = builder.comment("Weight of a cursed energy nature other than basic (1/value chance, bigger value = rarer)")
                .defineInRange("cursedEnergyNatureRarity", 15, 0, 1000000);
        this.curseRarity = builder.comment("Rarity of being a curse (1/value chance, bigger value = rarer")
                .defineInRange("curseRarity", 4, 0, 1000000);
        this.sixEyesWeight = builder.comment("Weight of having six eyes (lower value = rarer)")
                .defineInRange("sixEyesWeight", 4, 0, 1000000);
        this.heavenlyRestrictionRarity = builder.comment("Rarity of heavenly restriction (1/value chance, bigger value = rarer")
                .defineInRange("heavenlyRestrictionRarity", 30, 0, 1000000);
        this.vesselWeight = builder.comment("Weight of being a vessel (lower value = rarer)")
                .defineInRange("vesselWeight", 12, 0, 1000000);
        this.perfectBodyWeight = builder.comment("Weight of having a perfect body (lower value = rarer)")
                .defineInRange("perfectBodyWeight", 4, 0, 1000000);
        this.incarnatedWeight = builder.comment("Weight of being incarnated (lower value = rarer)")
                .defineInRange("incarnatedWeight", 36, 0, 1000000);
        this.rctOutputWeight = builder.comment("Weight of being able to output your RCT (lower value = rarer)")
                .defineInRange("rctOutputWeight", 30, 0, 1000000);
        this.prodigyWeight = builder.comment("Weight of having immense development potential (lower value = rarer)")
                .defineInRange("prodigyWeight", 8, 0, 1000000);
        this.cursedWombWeight = builder.comment("Weight of forming as a Cursed Womb (lower value = rarer)")
                .defineInRange("cursedWombWeight", 24, 0, 1000000);
        this.deathPaintingWeight = builder.comment("Weight of having been born as a Death Painting (lower value = rarer)")
                .defineInRange("deathPaintingWeight", 16, 0, 1000000);
        builder.pop();
    }


    

    public List<CursedTechnique> getUnlockableTechniques(JujutsuType type) {
        if (type == JujutsuType.SORCERER) {
                return this.unlockableSorcererTechniques.get().stream()
                .map(CursedTechnique::valueOf)
                .collect(Collectors.toList());
        }
        else {
                return this.unlockableCursedSpiritTechniques.get().stream()
                .map(CursedTechnique::valueOf)
                .collect(Collectors.toList());
        }
    }

    public List<Trait> getUniqueTraits() {
        return this.uniqueTraitList.get().stream()
                .map(Trait::valueOf)
                .collect(Collectors.toList());
    }

    public Map<Trait, Integer> getTraits(JujutsuType type) {
        Map<Trait, Integer> traitWeights = new HashMap<>();

        List<Trait> traits;
        if (type == JujutsuType.SORCERER) {
                traits = this.sorcererTraitList.get().stream()
                        .map(Trait::valueOf)
                        .collect(Collectors.toList());
        } else {
                traits = this.curseTraitList.get().stream()
                        .map(Trait::valueOf)
                        .collect(Collectors.toList());
        }

        for (Trait t : traits) {
                switch (t) {
                case VESSEL -> traitWeights.put(t, vesselWeight.get());
                case SIX_EYES -> traitWeights.put(t, sixEyesWeight.get());
                case RCT_OUTPUT -> traitWeights.put(t, rctOutputWeight.get());
                case INCARNATED -> traitWeights.put(t, incarnatedWeight.get());
                case DEATH_PAINTING -> traitWeights.put(t, deathPaintingWeight.get());
                case CURSED_WOMB -> traitWeights.put(t, cursedWombWeight.get());
                case PERFECT_BODY -> traitWeights.put(t, perfectBodyWeight.get());
                case PRODIGY -> traitWeights.put(t, prodigyWeight.get());
                }
        }

        return traitWeights;
}
    
}
