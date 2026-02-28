package com.stofiiis.highlife.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SerenityEffect extends MobEffect {
    public SerenityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4CAF50);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            player.getFoodData().eat(1, 0.05F + 0.02F * amplifier);
        }
        return true;
    }
}
