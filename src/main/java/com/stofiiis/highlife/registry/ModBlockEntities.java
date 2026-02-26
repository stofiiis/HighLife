package com.stofiiis.highlife.registry;

import java.util.Set;

import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.block.entity.DryingRackBlockEntity;
import com.stofiiis.highlife.block.entity.SeedMixerBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HighLifeMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK = BLOCK_ENTITY_TYPES.register(
            "drying_rack",
            () -> new BlockEntityType<>(DryingRackBlockEntity::new, Set.of(ModBlocks.DRYING_RACK.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeedMixerBlockEntity>> SEED_MIXER = BLOCK_ENTITY_TYPES.register(
            "seed_mixer",
            () -> new BlockEntityType<>(SeedMixerBlockEntity::new, Set.of(ModBlocks.SEED_MIXER.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
