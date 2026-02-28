package com.stofiiis.highlife.util;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stofiiis.highlife.HighLifeMod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public record StrainData(Strain strain, float quality) {
    private static final String TAG_STRAIN = HighLifeMod.MODID + ".strain";
    private static final String TAG_QUALITY = HighLifeMod.MODID + ".quality";

    public static final StrainData DEFAULT = new StrainData(Strain.VERDANT_BLOOM, 1.0F);

    public static final Codec<StrainData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Strain.CODEC.fieldOf("strain").forGetter(StrainData::strain),
            Codec.FLOAT.fieldOf("quality").forGetter(StrainData::quality))
            .apply(instance, StrainData::new));

    public StrainData {
        strain = Objects.requireNonNullElse(strain, Strain.VERDANT_BLOOM);
        quality = Mth.clamp(quality, 0.60F, 1.35F);
    }

    public static Optional<StrainData> get(ItemStack stack) {
        return get(stack, TAG_STRAIN, TAG_QUALITY);
    }

    public static Optional<StrainData> get(ItemStack stack, String strainKey, String qualityKey) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        String strainName = customData.copyTag().getStringOr(strainKey, "");
        if (strainName.isEmpty()) {
            return Optional.empty();
        }

        Strain strain = Strain.byName(strainName);
        float quality = customData.copyTag().getFloatOr(qualityKey, 1.0F);
        return Optional.of(new StrainData(strain, quality));
    }

    public static StrainData getOrCreate(ItemStack stack, RandomSource random) {
        Optional<StrainData> existing = get(stack);
        if (existing.isPresent()) {
            return existing.get();
        }

        StrainData generated = random(random);
        set(stack, generated);
        return generated;
    }

    public static StrainData getOrDefault(ItemStack stack) {
        return get(stack).orElse(DEFAULT);
    }

    public static StrainData random(RandomSource random) {
        Strain[] values = Strain.values();
        Strain strain = values[random.nextInt(values.length)];
        float quality = 0.82F + random.nextFloat() * 0.36F;
        return new StrainData(strain, quality);
    }

    public StrainData mutate(RandomSource random, float mutationChance) {
        Strain nextStrain = this.strain;
        if (random.nextFloat() < Mth.clamp(mutationChance, 0.0F, 1.0F)) {
            Strain[] values = Strain.values();
            nextStrain = values[random.nextInt(values.length)];
        }

        float drift = (random.nextFloat() - 0.5F) * 0.16F;
        return new StrainData(nextStrain, Mth.clamp(this.quality + drift, 0.60F, 1.35F));
    }

    public StrainData withQualityOffset(float offset) {
        return new StrainData(this.strain, this.quality + offset);
    }

    public float potencyMultiplier() {
        float qualityFactor = Mth.clamp(0.85F + (this.quality - 1.0F) * 0.80F, 0.65F, 1.30F);
        return this.strain.potencyModifier * qualityFactor;
    }

    public float durationMultiplier() {
        float qualityFactor = Mth.clamp(0.92F + (this.quality - 1.0F) * 0.65F, 0.75F, 1.25F);
        return this.strain.durationModifier * qualityFactor;
    }

    public float harshnessMultiplier() {
        float qualityFactor = Mth.clamp(1.0F + (1.0F - this.quality) * 0.35F, 0.82F, 1.24F);
        return this.strain.harshnessModifier * qualityFactor;
    }

    public int potencyPercent() {
        return Math.round(this.potencyMultiplier() * 100.0F);
    }

    public Component strainNameComponent() {
        return Component.translatable("strain.highlife." + this.strain.getSerializedName())
                .withStyle(style -> style.withColor(this.strain.color));
    }

    public Component qualityNameComponent() {
        return Component.translatable(this.qualityTranslationKey())
                .withStyle(style -> style.withColor(this.qualityColor()));
    }

    private String qualityTranslationKey() {
        if (this.quality < 0.78F) {
            return "tooltip.highlife.quality.low";
        }
        if (this.quality < 0.95F) {
            return "tooltip.highlife.quality.common";
        }
        if (this.quality < 1.12F) {
            return "tooltip.highlife.quality.premium";
        }
        return "tooltip.highlife.quality.exotic";
    }

    private int qualityColor() {
        if (this.quality < 0.78F) {
            return 0xD46A6A;
        }
        if (this.quality < 0.95F) {
            return 0xBDBDBD;
        }
        if (this.quality < 1.12F) {
            return 0x55C8A8;
        }
        return 0x6CD5FF;
    }

    public static void set(ItemStack stack, StrainData strainData) {
        set(stack, TAG_STRAIN, TAG_QUALITY, strainData);
    }

    public static void set(ItemStack stack, String strainKey, String qualityKey, StrainData strainData) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(strainKey, strainData.strain.getSerializedName());
            tag.putFloat(qualityKey, strainData.quality);
        });
    }

    public static void clear(ItemStack stack) {
        clear(stack, TAG_STRAIN, TAG_QUALITY);
    }

    public static void clear(ItemStack stack, String strainKey, String qualityKey) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(strainKey);
            tag.remove(qualityKey);
        });
    }

    public static void copy(ItemStack source, ItemStack destination) {
        get(source).ifPresent(data -> set(destination, data));
    }

    public enum Strain implements StringRepresentable {
        VERDANT_BLOOM("verdant_bloom", 0x4CAF50, 1.08F, 1.03F, 1.00F),
        GOLDEN_MIST("golden_mist", 0xE2C84D, 1.16F, 0.93F, 1.14F),
        AZURE_GLOW("azure_glow", 0x5EA8FF, 1.00F, 1.14F, 0.90F),
        SILVER_SPARK("silver_spark", 0xEAEAEA, 1.12F, 1.00F, 1.05F),
        LUMEN_TRAIL("lumen_trail", 0xA66CFF, 0.96F, 1.20F, 0.84F);

        public static final Codec<Strain> CODEC = StringRepresentable.fromEnum(Strain::values);

        private final String id;
        private final int color;
        private final float potencyModifier;
        private final float durationModifier;
        private final float harshnessModifier;

        Strain(String id, int color, float potencyModifier, float durationModifier, float harshnessModifier) {
            this.id = id;
            this.color = color;
            this.potencyModifier = potencyModifier;
            this.durationModifier = durationModifier;
            this.harshnessModifier = harshnessModifier;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        public static Strain byName(String name) {
            String normalized = name.toLowerCase(Locale.ROOT);
            for (Strain strain : values()) {
                if (strain.id.equals(normalized)) {
                    return strain;
                }
            }
            return VERDANT_BLOOM;
        }
    }
}
