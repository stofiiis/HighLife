package com.stofiiis.highlife.registry;

import com.stofiiis.highlife.HighLifeMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, HighLifeMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> JOINT_PUFF = register("joint_puff");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_PUFF = register("pipe_puff");
    public static final DeferredHolder<SoundEvent, SoundEvent> BONG_PUFF = register("bong_puff");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(HighLifeMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
