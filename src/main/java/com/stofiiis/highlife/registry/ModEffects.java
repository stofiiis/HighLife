package com.stofiiis.highlife.registry;

import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.effect.ThirstEffect;
import com.stofiiis.highlife.effect.HazeEffect;
import com.stofiiis.highlife.effect.PeaceEffect;
import com.stofiiis.highlife.effect.SerenityEffect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, HighLifeMod.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> RELAXED = EFFECTS.register("serenity", SerenityEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> COTTONMOUTH = EFFECTS.register("thirst", ThirstEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FOG = EFFECTS.register("haze", HazeEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> PEACE = EFFECTS.register("peace", PeaceEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
