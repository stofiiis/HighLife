package com.stofiiis.weed.item;

import com.stofiiis.weed.config.WeedConfig;
import com.stofiiis.weed.registry.ModEffects;
import com.stofiiis.weed.registry.ModItems;
import com.stofiiis.weed.util.AdvancementTracker;
import com.stofiiis.weed.util.StrainData;
import com.stofiiis.weed.util.ToleranceData;

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

public class SmokeItem extends Item {
    private final float potency;
    private final int relaxedDuration;
    private final int cottonmouthDuration;
    private final int fogDuration;
    private final int toleranceGain;
    private final boolean grantsPeace;
    private final boolean usesStrainSystem;

    public SmokeItem(Properties properties, float potency, int relaxedDuration, int cottonmouthDuration, int fogDuration, int toleranceGain) {
        this(properties, potency, relaxedDuration, cottonmouthDuration, fogDuration, toleranceGain, true);
    }

    public SmokeItem(Properties properties, float potency, int relaxedDuration, int cottonmouthDuration, int fogDuration, int toleranceGain, boolean grantsPeace) {
        this(properties, potency, relaxedDuration, cottonmouthDuration, fogDuration, toleranceGain, grantsPeace, true);
    }

    public SmokeItem(Properties properties, float potency, int relaxedDuration, int cottonmouthDuration, int fogDuration, int toleranceGain, boolean grantsPeace, boolean usesStrainSystem) {
        super(properties);
        this.potency = potency;
        this.relaxedDuration = relaxedDuration;
        this.cottonmouthDuration = cottonmouthDuration;
        this.fogDuration = fogDuration;
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
            this.applySmokeEffects(player, hadRelaxedBeforeUse, strainData);
            this.emitSmokeFeedback(level, player, stack);
            AdvancementTracker.onSmokeUsed(player, stack);
            if (!hadPeaceBeforeUse && player.hasEffect(ModEffects.PEACE)) {
                AdvancementTracker.onPeaceApplied(player);
            }
            result = this.afterSmokeUse(result, level, player);
        }

        return result;
    }

    protected boolean consumesItemOnUse() {
        return true;
    }

    protected ItemStack afterSmokeUse(ItemStack stack, Level level, Player player) {
        return stack;
    }

    protected StrainData resolveStrainForUse(ItemStack stack, Player player) {
        if (!this.usesStrainSystem) {
            return StrainData.DEFAULT;
        }
        return StrainData.getOrCreate(stack, player.getRandom());
    }

    protected void applySmokeEffects(Player player, boolean hadRelaxedBeforeUse, StrainData strainData) {
        int tolerance = ToleranceData.get(player);
        float toleranceScale = Math.max(0.35F, 1.0F - tolerance * 0.006F);
        float durationMultiplier = WeedConfig.getDurationMultiplier();
        float potency = this.potency * strainData.potencyMultiplier();
        float strainDurationMultiplier = strainData.durationMultiplier();
        float harshnessMultiplier = strainData.harshnessMultiplier();

        int relaxedTicks = Math.max(160, Math.round(this.relaxedDuration * potency * toleranceScale * durationMultiplier * strainDurationMultiplier));
        int cottonmouthTicks = Math.max(120, Math.round(this.cottonmouthDuration * (0.85F + 0.25F * potency) * durationMultiplier * harshnessMultiplier));
        int fogTicks = Math.max(80, Math.round(this.fogDuration * (0.8F + 0.2F * potency) * durationMultiplier * harshnessMultiplier));

        int relaxedAmplifier = potency >= 1.5F ? 1 : 0;
        player.addEffect(new MobEffectInstance(ModEffects.RELAXED, relaxedTicks, relaxedAmplifier, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Math.max(100, relaxedTicks / 3), 0, true, false, true));

        float cottonmouthChance = Mth.clamp(0.78F * WeedConfig.getCottonmouthChanceMultiplier() * harshnessMultiplier, 0.0F, 1.0F);
        if (player.getRandom().nextFloat() < cottonmouthChance) {
            player.addEffect(new MobEffectInstance(ModEffects.COTTONMOUTH, cottonmouthTicks, potency >= 1.4F ? 1 : 0, true, true, true));
        }

        float fogChance = Mth.clamp((0.5F + 0.15F * potency) * WeedConfig.getFogChanceMultiplier() * harshnessMultiplier, 0.0F, 1.0F);
        if (player.getRandom().nextFloat() < fogChance) {
            player.addEffect(new MobEffectInstance(ModEffects.FOG, fogTicks, potency >= 1.6F ? 1 : 0, true, true, true));
        }

        if (this.grantsPeace) {
            int peaceTicks = WeedConfig.getPeaceDurationTicks();
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

    protected void emitSmokeFeedback(Level level, Player player, ItemStack stack) {
        if (WeedConfig.isSmokeSoundsEnabled()) {
            SoundEvent smokeSound = this.getSmokeSound(stack, player);
            level.playSound(null, player.blockPosition(), smokeSound, SoundSource.PLAYERS, 0.7F, 0.9F + player.getRandom().nextFloat() * 0.22F);
        }
    }

    protected SoundEvent getSmokeSound(ItemStack stack, Player player) {
        return SoundEvents.SMOKER_SMOKE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        if (!this.usesStrainSystem) {
            return;
        }

        StrainData.get(stack).ifPresent(data -> {
            tooltipAdder.accept(Component.translatable("tooltip.weed.strain", data.strainNameComponent()));
            tooltipAdder.accept(Component.translatable("tooltip.weed.quality", data.qualityNameComponent()));
            tooltipAdder.accept(Component.translatable("tooltip.weed.potency", data.potencyPercent()));
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
            if (inventoryStack.is(ModItems.DRIED_CANNABIS_BUD.get())) {
                StrainData data = StrainData.getOrCreate(inventoryStack, player.getRandom());
                inventoryStack.shrink(1);
                player.getInventory().setChanged();
                return data;
            }
        }
        return null;
    }
}
