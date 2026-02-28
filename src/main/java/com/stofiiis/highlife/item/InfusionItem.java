package com.stofiiis.highlife.item;

import com.stofiiis.highlife.config.HighLifeConfig;
import com.stofiiis.highlife.registry.ModEffects;
import com.stofiiis.highlife.registry.ModItems;
import com.stofiiis.highlife.util.AdvancementTracker;
import com.stofiiis.highlife.util.StrainData;
import com.stofiiis.highlife.util.ToleranceData;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.function.Consumer;

public class InfusionItem extends Item {
    private final float potency;
    private final int serenityDuration;
    private final int thirstDuration;
    private final int hazeDuration;
    private final int toleranceGain;
    private final boolean grantsPeace;
    private final boolean usesStrainSystem;

    public InfusionItem(Properties properties, float potency, int serenityDuration, int thirstDuration, int hazeDuration, int toleranceGain) {
        this(properties, potency, serenityDuration, thirstDuration, hazeDuration, toleranceGain, true);
    }

    public InfusionItem(Properties properties, float potency, int serenityDuration, int thirstDuration, int hazeDuration, int toleranceGain, boolean grantsPeace) {
        this(properties, potency, serenityDuration, thirstDuration, hazeDuration, toleranceGain, grantsPeace, true);
    }

    public InfusionItem(Properties properties, float potency, int serenityDuration, int thirstDuration, int hazeDuration, int toleranceGain, boolean grantsPeace, boolean usesStrainSystem) {
        super(properties);
        this.potency = potency;
        this.serenityDuration = serenityDuration;
        this.thirstDuration = thirstDuration;
        this.hazeDuration = hazeDuration;
        this.toleranceGain = toleranceGain;
        this.grantsPeace = grantsPeace;
        this.usesStrainSystem = usesStrainSystem;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 16;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        boolean hadRelaxedBeforeUse = livingEntity.hasEffect(ModEffects.RELAXED);
        boolean hadPeaceBeforeUse = livingEntity.hasEffect(ModEffects.PEACE);
        ItemStack result = this.consumesItemOnUse() ? super.finishUsingItem(stack, level, livingEntity) : stack;

        if (!level.isClientSide() && livingEntity instanceof Player player) {
            StrainData strainData = this.resolveStrainForUse(stack, player);
            this.applyInfusionEffects(player, hadRelaxedBeforeUse, strainData);
            this.emitInfusionFeedback(level, player, stack);
            AdvancementTracker.onInfusionUsed(player, stack);
            if (!hadPeaceBeforeUse && player.hasEffect(ModEffects.PEACE)) {
                AdvancementTracker.onPeaceApplied(player);
            }
            result = this.afterInfusionUse(result, level, player);
        }

        return result;
    }

    protected boolean consumesItemOnUse() {
        return true;
    }

    protected ItemStack afterInfusionUse(ItemStack stack, Level level, Player player) {
        return stack;
    }

    protected StrainData resolveStrainForUse(ItemStack stack, Player player) {
        if (!this.usesStrainSystem) {
            return StrainData.DEFAULT;
        }
        return StrainData.getOrCreate(stack, player.getRandom());
    }

    protected void applyInfusionEffects(Player player, boolean hadRelaxedBeforeUse, StrainData strainData) {
        int tolerance = ToleranceData.get(player);
        float toleranceScale = Math.max(0.35F, 1.0F - tolerance * 0.006F);
        float durationMultiplier = HighLifeConfig.getDurationMultiplier();
        float potency = this.potency * strainData.potencyMultiplier();
        float strainDurationMultiplier = strainData.durationMultiplier();
        float harshnessMultiplier = strainData.harshnessMultiplier();

        int serenityTicks = Math.max(160, Math.round(this.serenityDuration * potency * toleranceScale * durationMultiplier * strainDurationMultiplier));
        int thirstTicks = Math.max(120, Math.round(this.thirstDuration * (0.85F + 0.25F * potency) * durationMultiplier * harshnessMultiplier));
        int hazeTicks = Math.max(80, Math.round(this.hazeDuration * (0.8F + 0.2F * potency) * durationMultiplier * harshnessMultiplier));

        int serenityAmplifier = potency >= 1.5F ? 1 : 0;
        player.addEffect(new MobEffectInstance(ModEffects.RELAXED, serenityTicks, serenityAmplifier, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Math.max(100, serenityTicks / 3), 0, true, false, true));

        float thirstChance = Mth.clamp(0.78F * HighLifeConfig.getThirstChanceMultiplier() * harshnessMultiplier, 0.0F, 1.0F);
        if (player.getRandom().nextFloat() < thirstChance) {
            player.addEffect(new MobEffectInstance(ModEffects.COTTONMOUTH, thirstTicks, potency >= 1.4F ? 1 : 0, true, true, true));
        }

        float hazeChance = Mth.clamp((0.5F + 0.15F * potency) * HighLifeConfig.getHazeChanceMultiplier() * harshnessMultiplier, 0.0F, 1.0F);
        if (player.getRandom().nextFloat() < hazeChance) {
            player.addEffect(new MobEffectInstance(ModEffects.FOG, hazeTicks, potency >= 1.6F ? 1 : 0, true, true, true));
        }

        if (this.grantsPeace) {
            int peaceTicks = HighLifeConfig.getPeaceDurationTicks();
            if (peaceTicks > 0) {
                player.addEffect(new MobEffectInstance(ModEffects.PEACE, peaceTicks, 0, true, true, true));
            }
        }

        float overdoseChance = 0.10F + (tolerance * 0.0032F) + (potency * 0.07F);
        if (hadRelaxedBeforeUse && player.getRandom().nextFloat() < overdoseChance) {
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 220, 0, true, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 1, true, true, true));
        }

        ToleranceData.add(player, this.toleranceGain);
    }

    protected void emitInfusionFeedback(Level level, Player player, ItemStack stack) {
        if (HighLifeConfig.isInfusionSoundsEnabled()) {
            SoundEvent infusionSound = this.getInfusionSound(stack, player);
            level.playSound(null, player.blockPosition(), infusionSound, SoundSource.PLAYERS, 0.7F, 0.9F + player.getRandom().nextFloat() * 0.22F);
        }
    }

    protected SoundEvent getInfusionSound(ItemStack stack, Player player) {
        return SoundEvents.BREWING_STAND_BREW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        if (!this.usesStrainSystem) {
            return;
        }

        StrainData.get(stack).ifPresent(data -> {
            tooltipAdder.accept(Component.translatable("tooltip.highlife.strain", data.strainNameComponent()));
            tooltipAdder.accept(Component.translatable("tooltip.highlife.quality", data.qualityNameComponent()));
            tooltipAdder.accept(Component.translatable("tooltip.highlife.potency", data.potencyPercent()));
        });
    }

    protected static boolean getCustomBoolean(ItemStack stack, String key, boolean defaultValue) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getBooleanOr(key, defaultValue);
    }

    protected static void setCustomBoolean(ItemStack stack, String key, boolean value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (value) {
                tag.putBoolean(key, true);
            } else {
                tag.remove(key);
            }
        });
    }

    protected static int getCustomInt(ItemStack stack, String key, int defaultValue) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getIntOr(key, defaultValue);
    }

    protected static void setCustomInt(ItemStack stack, String key, int value, int removeWhenValue) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (value == removeWhenValue) {
                tag.remove(key);
            } else {
                tag.putInt(key, value);
            }
        });
    }

    public static StrainData consumeDriedBudFromInventory(Player player) {
        if (player.getAbilities().instabuild) {
            return StrainData.random(player.getRandom());
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack inventoryStack = player.getInventory().getItem(slot);
            if (inventoryStack.is(ModItems.DRIED_MYSTIC_HERB.get())) {
                StrainData data = StrainData.getOrCreate(inventoryStack, player.getRandom());
                inventoryStack.shrink(1);
                player.getInventory().setChanged();
                return data;
            }
        }
        return null;
    }
}
