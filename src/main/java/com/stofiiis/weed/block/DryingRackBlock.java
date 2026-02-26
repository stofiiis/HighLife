package com.stofiiis.weed.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.stofiiis.weed.block.entity.DryingRackBlockEntity;
import com.stofiiis.weed.registry.ModBlockEntities;
import com.stofiiis.weed.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class DryingRackBlock extends BaseEntityBlock {
    public static final MapCodec<DryingRackBlock> CODEC = simpleCodec(DryingRackBlock::new);
    public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");

    public DryingRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OCCUPIED, Boolean.FALSE));
    }

    @Override
    public MapCodec<DryingRackBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, ModBlockEntities.DRYING_RACK.get(), DryingRackBlockEntity::tickServer);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return handleRackInteraction(stack, player, rack);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return handleRackInteraction(ItemStack.EMPTY, player, rack);
    }

    private static InteractionResult handleRackInteraction(ItemStack heldStack, Player player, DryingRackBlockEntity rack) {
        if (rack.isEmpty()) {
            if (!heldStack.is(ModItems.CANNABIS_BUD.get())) {
                return InteractionResult.PASS;
            }

            rack.insertOne(heldStack);
            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }
            player.level().playSound(null, rack.getBlockPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.0F);
            int seconds = rack.getDryTimeTotal() / 20;
            player.displayClientMessage(Component.translatable("message.weed.drying_started", seconds), true);
            return InteractionResult.SUCCESS;
        }

        if (rack.isDryingComplete()) {
            ItemStack extracted = rack.extract();
            if (!player.addItem(extracted)) {
                player.drop(extracted, false);
            }
            player.level().playSound(null, rack.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.9F, 1.0F);
            player.displayClientMessage(Component.translatable("message.weed.drying_ready"), true);
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            ItemStack extracted = rack.extract();
            if (!player.addItem(extracted)) {
                player.drop(extracted, false);
            }
            player.level().playSound(null, rack.getBlockPos(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 0.95F);
            player.displayClientMessage(Component.translatable("message.weed.drying_cancelled"), true);
            return InteractionResult.SUCCESS;
        }

        int remainingSeconds = Math.max(1, rack.getRemainingTicks() / 20);
        player.displayClientMessage(Component.translatable("message.weed.drying_progress", rack.getProgressPercent(), remainingSeconds), true);
        return InteractionResult.SUCCESS;
    }
}
