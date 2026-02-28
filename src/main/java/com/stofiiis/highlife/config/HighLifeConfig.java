package com.stofiiis.highlife.config;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class HighLifeConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_SEED_DROPS;
    private static final ModConfigSpec.DoubleValue SEED_DROP_CHANCE;

    private static final ModConfigSpec.IntValue TOLERANCE_DECAY_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue TOLERANCE_DECAY_AMOUNT;

    private static final ModConfigSpec.IntValue DRY_TIME_TICKS;
    private static final ModConfigSpec.BooleanValue DRYING_RACK_PARTICLES;

    private static final ModConfigSpec.DoubleValue DURATION_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue THIRST_CHANCE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue HAZE_CHANCE_MULTIPLIER;
    private static final ModConfigSpec.IntValue PEACE_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue INFUSION_PARTICLES;
    private static final ModConfigSpec.BooleanValue INFUSION_SOUNDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("drops");
        ENABLE_SEED_DROPS = builder
                .comment("If false, grass and ferns will not drop mystic_herb seeds.")
                .define("enableSeedDrops", true);
        SEED_DROP_CHANCE = builder
                .comment("Chance for one mystic_herb seed drop when breaking grass-like plants.")
                .defineInRange("seedDropChance", 0.18D, 0.0D, 1.0D);
        builder.pop();

        builder.push("tolerance");
        TOLERANCE_DECAY_INTERVAL_TICKS = builder
                .comment("How often tolerance decays (in ticks). 20 ticks = 1 second.")
                .defineInRange("decayIntervalTicks", 1200, 20, 72000);
        TOLERANCE_DECAY_AMOUNT = builder
                .comment("How much tolerance is removed each decay tick.")
                .defineInRange("decayAmount", 1, 0, 20);
        builder.pop();

        builder.push("drying_rack");
        DRY_TIME_TICKS = builder
                .comment("How many ticks drying one mystic_herb bud takes.")
                .defineInRange("dryTimeTicks", 1200, 20, 72000);
        DRYING_RACK_PARTICLES = builder
                .comment("Show visual particles while drying and on completion.")
                .define("enableParticles", true);
        builder.pop();

        builder.push("infusion");
        DURATION_MULTIPLIER = builder
                .comment("Global multiplier for Serenity/Thirst/Haze duration.")
                .defineInRange("durationMultiplier", 1.0D, 0.1D, 4.0D);
        THIRST_CHANCE_MULTIPLIER = builder
                .comment("Global multiplier for thirst chance.")
                .defineInRange("thirstChanceMultiplier", 1.0D, 0.0D, 3.0D);
        HAZE_CHANCE_MULTIPLIER = builder
                .comment("Global multiplier for haze chance.")
                .defineInRange("hazeChanceMultiplier", 1.0D, 0.0D, 3.0D);
        PEACE_DURATION_TICKS = builder
                .comment("How long Peace lasts after infusion use (in ticks). 600 = 30 seconds.")
                .defineInRange("peaceDurationTicks", 600, 0, 12000);
        INFUSION_PARTICLES = builder
                .comment("Spawn visual particles when infusion items are used.")
                .define("enableParticles", true);
        INFUSION_SOUNDS = builder
                .comment("Play infusion sound when infusion items are used.")
                .define("enableSounds", true);
        builder.pop();

        SPEC = builder.build();
    }

    private HighLifeConfig() {
    }

    public static boolean areSeedDropsEnabled() {
        return ENABLE_SEED_DROPS.get();
    }

    public static float getSeedDropChance() {
        return (float) Mth.clamp(SEED_DROP_CHANCE.get(), 0.0D, 1.0D);
    }

    public static int getToleranceDecayIntervalTicks() {
        return Math.max(20, TOLERANCE_DECAY_INTERVAL_TICKS.get());
    }

    public static int getToleranceDecayAmount() {
        return Math.max(0, TOLERANCE_DECAY_AMOUNT.get());
    }

    public static int getDryTimeTicks() {
        return Math.max(20, DRY_TIME_TICKS.get());
    }

    public static boolean isDryingRackParticlesEnabled() {
        return DRYING_RACK_PARTICLES.get();
    }

    public static float getDurationMultiplier() {
        return (float) Mth.clamp(DURATION_MULTIPLIER.get(), 0.1D, 4.0D);
    }

    public static float getThirstChanceMultiplier() {
        return (float) Mth.clamp(THIRST_CHANCE_MULTIPLIER.get(), 0.0D, 3.0D);
    }

    public static float getHazeChanceMultiplier() {
        return (float) Mth.clamp(HAZE_CHANCE_MULTIPLIER.get(), 0.0D, 3.0D);
    }

    public static int getPeaceDurationTicks() {
        return Math.max(0, PEACE_DURATION_TICKS.get());
    }

    public static boolean isInfusionParticlesEnabled() {
        return INFUSION_PARTICLES.get();
    }

    public static boolean isInfusionSoundsEnabled() {
        return INFUSION_SOUNDS.get();
    }
}
