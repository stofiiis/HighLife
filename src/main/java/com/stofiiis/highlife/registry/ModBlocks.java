package com.stofiiis.highlife.registry;

import com.stofiiis.highlife.HighLifeMod;
import com.stofiiis.highlife.block.CannabisCropBlock;
import com.stofiiis.highlife.block.DryingRackBlock;
import com.stofiiis.highlife.block.SeedMixerBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HighLifeMod.MODID);

    public static final DeferredBlock<CannabisCropBlock> CANNABIS_CROP = BLOCKS.registerBlock("cannabis_crop",
            CannabisCropBlock::new,
            properties -> properties.ofFullCopy(Blocks.BEETROOTS)
                    .noCollision()
                    .instabreak()
                    .randomTicks()
                    .sound(SoundType.CROP));

    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = BLOCKS.registerBlock("drying_rack",
            DryingRackBlock::new,
            properties -> properties.mapColor(MapColor.WOOD)
                    .strength(1.2F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<SeedMixerBlock> SEED_MIXER = BLOCKS.registerBlock("seed_mixer",
            SeedMixerBlock::new,
            properties -> properties.mapColor(MapColor.STONE)
                    .strength(2.0F)
                    .sound(SoundType.STONE));

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
