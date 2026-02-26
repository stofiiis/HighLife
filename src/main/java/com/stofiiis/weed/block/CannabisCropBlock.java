package com.stofiiis.weed.block;

import com.mojang.serialization.MapCodec;
import com.stofiiis.weed.registry.ModItems;
import com.stofiiis.weed.util.CropGeneticsData;
import com.stofiiis.weed.util.SeedCategoryData;
import com.stofiiis.weed.util.StrainData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.tryHarvestMature(state, level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        return this.tryHarvestMature(state, level, pos);
    }

    private InteractionResult tryHarvestMature(BlockState state, Level level, BlockPos pos) {
        if (this.getAge(state) < this.getMaxAge()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        RandomSource random = serverLevel.getRandom();
        StrainData source = CropGeneticsData.get(serverLevel).get(pos).orElseGet(() -> StrainData.random(random));

        int budCount = 1 + random.nextInt(2);
        ItemStack budDrop = new ItemStack(ModItems.CANNABIS_BUD.get(), budCount);
        StrainData budData = source.mutate(random, 0.08F).withQualityOffset(0.02F);
        StrainData.set(budDrop, budData);
        Block.popResource(serverLevel, pos, budDrop);

        StrainData seedData = source.mutate(random, 0.16F);
        SeedCategoryData.SeedCategory category = SeedCategoryData.fromQuality(seedData.quality());
        int seedCount = 1 + (random.nextFloat() < 0.55F ? 1 : 0) + (random.nextFloat() < 0.15F ? 1 : 0);
        ItemStack seedDrop = new ItemStack(ModItems.CANNABIS_SEEDS.get(), seedCount);
        SeedCategoryData.set(seedDrop, category);
        StrainData.set(seedDrop, SeedCategoryData.stackableSeedData(category, seedData.strain()));
        Block.popResource(serverLevel, pos, seedDrop);

        CropGeneticsData.get(serverLevel).put(pos, source.mutate(random, 0.05F));
        serverLevel.setBlock(pos, state.setValue(this.getAgeProperty(), 1), 2);
        serverLevel.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 0.95F + random.nextFloat() * 0.1F);
        return InteractionResult.SUCCESS;
    }
}
