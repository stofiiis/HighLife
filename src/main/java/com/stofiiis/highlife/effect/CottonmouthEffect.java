package com.stofiiis.highlife.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CottonmouthEffect extends MobEffect {
    public CottonmouthEffect() {
        super(MobEffectCategory.HARMFUL, 0xB57F50);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 50 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            player.causeFoodExhaustion(0.6F + amplifier * 0.25F);
        }
        return true;
    }
}
