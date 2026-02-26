package com.stofiiis.highlife.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class FogEffect extends MobEffect {
    public FogEffect() {
        super(MobEffectCategory.HARMFUL, 0x90A4AE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 80 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        if (livingEntity.getRandom().nextFloat() < 0.24F + 0.08F * amplifier) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50 + amplifier * 20, 0, true, false, true));
        }
        return true;
    }
}
