package com.stofiiis.weed.block;

import com.mojang.serialization.MapCodec;
import com.stofiiis.weed.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CannabisCropBlock extends CropBlock {
    public static final MapCodec<CannabisCropBlock> CODEC = simpleCodec(CannabisCropBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
            box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
            box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D),
            box(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D),
            box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)
    };

    public CannabisCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(getAgeProperty(), 0));
    }

    @Override
    public MapCodec<CannabisCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected Item getBaseSeedId() {
        return ModItems.CANNABIS_SEEDS.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(this.getAgeProperty())];
    }
}
