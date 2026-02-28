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

    public static final DeferredHolder<SoundEvent, SoundEvent> ROLL_CAST = register("roll_cast");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND_CHARGE = register("wand_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLASK_BUBBLE = register("flask_bubble");

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
