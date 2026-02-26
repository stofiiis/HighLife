package com.stofiiis.weed.util;

import java.util.Optional;

import com.stofiiis.weed.WeedMod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class SeedCategoryData {
    private static final String TAG_SEED_CATEGORY = WeedMod.MODID + ".seed_category";
    private static final float TOTAL_DROP_WEIGHT = SeedCategory.totalDropWeight();

    private SeedCategoryData() {
    }

    public static Optional<SeedCategory> get(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        String categoryName = customData.copyTag().getStringOr(TAG_SEED_CATEGORY, "");
        if (categoryName.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(SeedCategory.byName(categoryName));
    }

    public static void set(ItemStack stack, SeedCategory category) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(TAG_SEED_CATEGORY, category.getSerializedName()));
    }

    public static void clear(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(TAG_SEED_CATEGORY));
    }

    public static SeedCategory randomWildDrop(RandomSource random) {
        float roll = random.nextFloat() * TOTAL_DROP_WEIGHT;
        for (SeedCategory category : SeedCategory.values()) {
            roll -= category.dropWeight();
            if (roll <= 0.0F) {
                return category;
            }
        }
        return SeedCategory.COMMON;
    }

    public static SeedCategory fromQuality(float quality) {
        SeedCategory result = SeedCategory.COMMON;
        for (SeedCategory category : SeedCategory.values()) {
            if (quality >= category.minQuality()) {
                result = category;
            } else {
                break;
            }
        }
        return result;
    }

    public static StrainData rollPlantData(SeedCategory category, RandomSource random) {
        StrainData.Strain[] strains = StrainData.Strain.values();
        StrainData.Strain strain = strains[random.nextInt(strains.length)];
        float qualitySpread = category.maxQuality() - category.minQuality();
        float quality = category.minQuality() + random.nextFloat() * qualitySpread;
        return new StrainData(strain, quality);
    }

    public enum SeedCategory implements StringRepresentable {
        COMMON("common", 0xC8C8C8, 0.82F, 0.90F, 48.0F),
        UNCOMMON("uncommon", 0x77D17E, 0.90F, 0.98F, 24.0F),
        RARE("rare", 0x5FC6FF, 0.98F, 1.05F, 13.0F),
        EPIC("epic", 0xB58CFF, 1.05F, 1.12F, 7.0F),
        LEGENDARY("legendary", 0xFFB34D, 1.12F, 1.20F, 4.0F),
        MYTHIC("mythic", 0xFF6B9F, 1.20F, 1.28F, 2.5F),
        PREMIUM("premium", 0xFFD94D, 1.28F, 1.35F, 1.5F);

        private final String id;
        private final int color;
        private final float minQuality;
        private final float maxQuality;
        private final float dropWeight;

        SeedCategory(String id, int color, float minQuality, float maxQuality, float dropWeight) {
            this.id = id;
            this.color = color;
            this.minQuality = minQuality;
            this.maxQuality = maxQuality;
            this.dropWeight = dropWeight;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        public int color() {
            return this.color;
        }

        public float minQuality() {
            return this.minQuality;
        }

        public float maxQuality() {
            return this.maxQuality;
        }

        public float dropWeight() {
            return this.dropWeight;
        }

        public Component displayNameComponent() {
            return Component.translatable("seed_class.weed." + this.id)
                    .withStyle(style -> style.withColor(this.color));
        }

        public static SeedCategory byName(String name) {
            for (SeedCategory category : values()) {
                if (category.id.equals(name)) {
                    return category;
                }
            }
            return COMMON;
        }

        private static float totalDropWeight() {
            float total = 0.0F;
            for (SeedCategory category : values()) {
                total += category.dropWeight;
            }
            return total;
        }
    }
}
